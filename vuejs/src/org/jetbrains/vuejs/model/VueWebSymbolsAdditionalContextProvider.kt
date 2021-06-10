// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_ATTRIBUTES
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_EVENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_SLOTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_COMPONENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_COMPONENT_PROPS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_DIRECTIVES
import com.intellij.javascript.web.symbols.WebSymbol.Companion.VUE_FRAMEWORK
import com.intellij.javascript.web.symbols.WebSymbol.Priority
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.codeInsight.toAsset
import java.util.*

class VueWebSymbolsAdditionalContextProvider : WebSymbolsAdditionalContextProvider {

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> =
    element
      ?.takeIf { framework == VUE_FRAMEWORK }
      ?.let { VueModelManager.findEnclosingContainer(it) }
      ?.let { listOf(EntityContainerWrapper(element.containingFile.originalFile, it)) }
    ?: emptyList()

  private abstract class VueWrapperBase : WebSymbolsContainer,
                                          WebSymbolsContainer.Context {
    val context: WebSymbolsContainer.Context
      get() = this

    val namespace: WebSymbolsContainer.Namespace
      get() = WebSymbolsContainer.Namespace.HTML

    override val framework: FrameworkId?
      get() = VUE_FRAMEWORK

    override val packageName: String
      get() = "Vue project source"

    override val version: String?
      get() = null
  }

  private class EntityContainerWrapper(private val containingFile: PsiFile,
                                       private val container: VueEntitiesContainer) : VueWrapperBase() {

    override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                            kind: String,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): Sequence<WebSymbolsContainer> =
      if (namespace == null || namespace == WebSymbolsContainer.Namespace.HTML)
        when (kind) {
          KIND_HTML_VUE_COMPONENTS -> {
            val result = mutableListOf<VueComponent>()
            val normalizedTagName = name?.let { fromAsset(it) }
            container.acceptEntities(object : VueModelProximityVisitor() {
              override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
                return acceptSameProximity(proximity, normalizedTagName == null || fromAsset(name) == normalizedTagName) {
                  // Cannot self refer without export declaration with component name
                  if ((component.source as? JSImplicitElement)?.context != containingFile) {
                    result.add(component)
                  }
                }
              }
            }, VueModelVisitor.Proximity.GLOBAL)
            result.asSequence().mapNotNull {
              ComponentWrapper(name ?: it.defaultName ?: return@mapNotNull null, it)
            }
          }
          KIND_HTML_VUE_DIRECTIVES -> {
            val searchName = name?.let { fromAsset(it) }
            val directives = mutableListOf<VueDirective>()
            container.acceptEntities(object : VueModelProximityVisitor() {
              override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
                return acceptSameProximity(proximity, searchName == null || fromAsset(name) == searchName) {
                  directives.add(directive)
                }
              }
            }, VueModelVisitor.Proximity.GLOBAL)
            directives.asSequence().mapNotNull {
              DirectiveWrapper(name ?: it.defaultName ?: return@mapNotNull null, it)
            }
          }
          else -> emptySequence()
        }
      else emptySequence()

    override fun getCodeCompletions(namespace: WebSymbolsContainer.Namespace?,
                                    kind: String,
                                    name: String?,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    context: Stack<WebSymbolsContainer>): Sequence<WebSymbolCodeCompletionItem> =
      if (namespace == null || namespace == WebSymbolsContainer.Namespace.HTML)
        when (kind) {
          KIND_HTML_VUE_COMPONENTS -> {
            val result = mutableListOf<WebSymbolCodeCompletionItem>()
            val scriptLanguage = detectVueScriptLanguage(containingFile)
            container.acceptEntities(object : VueModelVisitor() {
              override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
                // Cannot self refer without export declaration with component name
                if ((component.source as? JSImplicitElement)?.context == containingFile) {
                  return true
                }
                val moduleName: String? = if (component.parents.size == 1) {
                  (component.parents.first() as? VuePlugin)?.moduleName
                }
                else null
                listOf(toAsset(name).capitalize(), fromAsset(name)).forEach {
                  result.add(createVueLookup(component, it, scriptLanguage, proximity, moduleName))
                }
                return true
              }
            }, VueModelVisitor.Proximity.OUT_OF_SCOPE)
            result.asSequence()
          }
          KIND_HTML_VUE_DIRECTIVES -> {
            val result = mutableListOf<WebSymbolCodeCompletionItem>()
            container.acceptEntities(object : VueModelVisitor() {
              override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
                result.add(WebSymbolCodeCompletionItem.create(fromAsset(name),
                                                              source = DirectiveWrapper(name, directive),
                                                              priority = priorityOf(proximity),
                                                              proximity = proximityOf(proximity)))
                return true
              }
            }, VueModelVisitor.Proximity.GLOBAL)
            result.asSequence()
          }
          else -> emptySequence()
        }
      else emptySequence()

    override fun getModificationCount(): Long =
      PsiModificationTracker.SERVICE.getInstance(containingFile.project).modificationCount

    private fun priorityOf(proximity: VueModelVisitor.Proximity): Priority =
      when (proximity) {
        VueModelVisitor.Proximity.LOCAL -> Priority.HIGHEST
        VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.APP -> Priority.HIGH
        VueModelVisitor.Proximity.GLOBAL -> Priority.HIGH
        VueModelVisitor.Proximity.OUT_OF_SCOPE -> Priority.NORMAL
      }

    private fun proximityOf(proximity: VueModelVisitor.Proximity): Int =
      when (proximity) {
        VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.APP -> 1
        else -> 0
      }

    private fun createVueLookup(component: VueComponent,
                                name: String,
                                scriptLanguage: String?,
                                proximity: VueModelVisitor.Proximity,
                                moduleName: String? = null): WebSymbolCodeCompletionItem {
      val element = component.source
      var builder = WebSymbolCodeCompletionItem.create(
        name = name,
        source = ComponentWrapper(name, component),
        icon = VuejsIcons.Vue,
        typeText = moduleName,
        priority = priorityOf(proximity),
        proximity = proximityOf(proximity))

      if (proximity == VueModelVisitor.Proximity.OUT_OF_SCOPE && element != null) {
        val settings = JSApplicationSettings.getInstance()
        if ((scriptLanguage != null && "ts" == scriptLanguage)
            || (DialectDetector.isTypeScript(element)
                && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
          if (settings.hasTSImportCompletionEffective(element.project)) {
            builder = builder.withInsertHandlerAdded(VueInsertHandler.INSTANCE)
          }
        }
        else {
          if (settings.isUseJavaScriptAutoImport) {
            builder = builder.withInsertHandlerAdded(VueInsertHandler.INSTANCE)
          }
        }
      }
      return builder
    }
  }

  private abstract class DocumentedItemWrapper<T : VueDocumentedItem>(
    override val matchedName: String, protected val item: T) : VueWrapperBase(), WebSymbol {

    override val description: String?
      get() = item.documentation.description

    override val docUrl: String?
      get() = item.documentation.docUrl

    override fun equals(other: Any?): Boolean =
      other is DocumentedItemWrapper<*>
      && matchedName == other.matchedName
      && item == other.item

    override fun hashCode(): Int = Objects.hash(matchedName, item)
  }

  private abstract class NamedSymbolWrapper<T : VueNamedSymbol>(item: T, matchedName: String = item.name) : DocumentedItemWrapper<T>(
    matchedName, item) {
    override val name: String
      get() = item.name

    override val source: PsiElement?
      get() = item.source
  }

  private class ComponentWrapper(matchedName: String, component: VueComponent) :
    DocumentedItemWrapper<VueComponent>(matchedName, component) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_COMPONENTS

    override val name: String
      get() = item.defaultName ?: matchedName

    override val source: PsiElement?
      get() = item.source

    override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                            kind: String,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): Sequence<WebSymbolsContainer> =
      if (namespace == null || namespace == WebSymbolsContainer.Namespace.HTML)
        when (kind) {
          KIND_HTML_VUE_COMPONENT_PROPS -> {
            val searchName = name?.let { fromAsset(it) }
            val props = mutableListOf<VueInputProperty>()
            item.acceptPropertiesAndMethods(object : VueModelVisitor() {
              override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
                if (searchName == null || fromAsset(prop.name) == searchName) {
                  props.add(prop)
                }
                return true
              }
            })
            props.asSequence().map { InputPropWrapper(name ?: it.name, it) }
          }
          KIND_HTML_EVENTS -> {
            (item as? VueContainer)?.emits?.asSequence()?.map { EmitCallWrapper(it) } ?: emptySequence()
          }
          KIND_HTML_SLOTS -> {
            (item as? VueContainer)?.slots?.asSequence()?.map { SlotWrapper(it) } ?: emptySequence()
          }
          else -> emptySequence()
        }
      else emptySequence()

  }

  private class InputPropWrapper(matchedName: String, property: VueInputProperty)
    : NamedSymbolWrapper<VueInputProperty>(property, matchedName) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_COMPONENT_PROPS

    override val jsType: JSType?
      get() = item.jsType
  }

  private class EmitCallWrapper(emitCall: VueEmitCall) : NamedSymbolWrapper<VueEmitCall>(emitCall) {

    override val kind: SymbolKind
      get() = KIND_HTML_EVENTS

    override val jsType: JSType?
      get() = item.eventJSType
  }

  private class SlotWrapper(slot: VueSlot) : NamedSymbolWrapper<VueSlot>(slot) {

    override val kind: SymbolKind
      get() = KIND_HTML_SLOTS

    override val jsType: JSType?
      get() = item.scope
  }

  private class DirectiveWrapper(matchedName: String, directive: VueDirective) :
    DocumentedItemWrapper<VueDirective>(matchedName, directive) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_DIRECTIVES

    override val name: String
      get() = item.defaultName ?: matchedName

    override val source: PsiElement?
      get() = item.source

  }

}
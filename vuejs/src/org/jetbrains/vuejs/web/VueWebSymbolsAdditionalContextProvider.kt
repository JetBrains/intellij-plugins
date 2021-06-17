// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_EVENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_SLOTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_COMPONENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_COMPONENT_PROPS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_VUE_DIRECTIVES
import com.intellij.javascript.web.symbols.WebSymbol.Companion.VUE_FRAMEWORK
import com.intellij.javascript.web.symbols.WebSymbol.Priority
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Companion.NAMESPACE_HTML
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Namespace
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_EVENT
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_PROP
import java.util.*

class VueWebSymbolsAdditionalContextProvider : WebSymbolsAdditionalContextProvider {

  companion object {
    const val KIND_VUE_TOP_LEVEL_ELEMENT = "vue-file-top-elements"
    const val KIND_VUE_AVAILABLE_SLOTS = "vue-available-slots"
    const val KIND_VUE_MODEL = "vue-model"
    const val KIND_VUE_DIRECTIVE_ARGUMENT = "argument"
    const val KIND_VUE_DIRECTIVE_MODIFIERS = "modifiers"

    const val PROP_VUE_MODEL_PROP = "prop"
    const val PROP_VUE_MODEL_EVENT = "event"

    private fun <T> List<T>.mapWithNameFilter(name: String?, mapper: (T) -> WebSymbol): List<WebSymbol> =
      if (name != null) {
        asSequence()
          .map(mapper)
          .filter { it.name == name }
          .toList()
      }
      else this.map(mapper)

  }

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> =
    element
      ?.takeIf { framework == VUE_FRAMEWORK }
      ?.let { VueModelManager.findEnclosingContainer(it) }
      ?.let {
        listOfNotNull(EntityContainerWrapper(element.containingFile.originalFile, it,
                                             (element as? XmlTag)?.let { tag -> tag.parentTag == null } == true),
                      (element as? XmlTag)?.let { tag -> AvailableSlotsContainer(tag) })
      }
    ?: emptyList()

  private abstract class VueWrapperBase : WebSymbolsContainer {

    val namespace: Namespace
      get() = Namespace.HTML

  }

  private class EntityContainerWrapper(private val containingFile: PsiFile,
                                       private val container: VueEntitiesContainer,
                                       private val isTopLevelTag: Boolean) : VueWrapperBase() {

    override fun getSymbols(namespace: Namespace?,
                            kind: String,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if (namespace == null || namespace == Namespace.HTML)
        when (kind) {
          KIND_HTML_ELEMENTS -> {
            if (containingFile.virtualFile?.fileType == VueFileType.INSTANCE && isTopLevelTag) {
              params.registry.runNameMatchQuery(
                if (name == null) listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENT)
                else listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENT, name)
              )
                .map {
                  WebSymbolMatch(it.name, it.nameSegments, Namespace.HTML, KIND_HTML_ELEMENTS, it.context)
                }
            }
            else emptyList()
          }
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
            result.mapNotNull {
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
            directives.mapNotNull {
              DirectiveWrapper(name ?: it.defaultName ?: return@mapNotNull null, it)
            }
          }
          else -> emptyList()
        }
      else emptyList()

    override fun getCodeCompletions(namespace: Namespace?,
                                    kind: String,
                                    name: String?,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
      if (namespace == null || namespace == Namespace.HTML)
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
                // TODO replace with params.registry.getNameVariants(VUE_FRAMEWORK, Namespace.HTML, kind, name)
                listOf(toAsset(name).capitalize(), fromAsset(name)).forEach {
                  result.add(createVueLookup(component, it, scriptLanguage, proximity))
                }
                return true
              }
            }, VueModelVisitor.Proximity.OUT_OF_SCOPE)
            result
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
            result
          }
          else -> emptyList()
        }
      else emptyList()

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
                                proximity: VueModelVisitor.Proximity): WebSymbolCodeCompletionItem {
      val element = component.source
      val wrapper = ComponentWrapper(name, component)
      var builder = WebSymbolCodeCompletionItem.create(
        name = name,
        source = wrapper,
        typeText = wrapper.context.packageName,
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

  private class AvailableSlotsContainer(private val tag: XmlTag) : WebSymbolsContainer {
    override fun getModificationCount(): Long = 0
    override fun getSymbols(namespace: Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if ((namespace == null || namespace == Namespace.HTML) && kind == KIND_VUE_AVAILABLE_SLOTS)
        getAvailableSlots(tag, name, true)
      else emptyList()
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

  private abstract class NamedSymbolWrapper<T : VueNamedSymbol>(item: T, matchedName: String = item.name,
                                                                override val context: WebSymbolsContainer.Context)
    : DocumentedItemWrapper<T>(matchedName, item) {

    override val name: String
      get() = item.name

    override val source: PsiElement?
      get() = item.source
  }

  private abstract class ScopeElementWrapper<T : VueDocumentedItem>(matchedName: String, item: T) :
    DocumentedItemWrapper<T>(matchedName, item) {

    override val context: WebSymbolsContainer.Context =
      object : WebSymbolsContainer.Context {

        override val framework: FrameworkId
          get() = VUE_FRAMEWORK

        override val packageName: String?
          get() = (item as VueScopeElement).parents
            .takeIf { it.size == 1 }
            ?.get(0)
            ?.castSafelyTo<VuePlugin>()
            ?.moduleName

        override val version: String?
          get() = (item as VueScopeElement).parents
            .takeIf { it.size == 1 }
            ?.get(0)
            ?.castSafelyTo<VuePlugin>()
            ?.moduleVersion
      }
  }

  private class ComponentWrapper(matchedName: String, component: VueComponent) :
    ScopeElementWrapper<VueComponent>(matchedName, component) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_COMPONENTS

    override val name: String
      get() = item.defaultName ?: matchedName

    override val source: PsiElement?
      get() = item.source

    override fun getSymbols(namespace: Namespace?,
                            kind: String,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if (namespace == null || namespace == Namespace.HTML)
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
            props.map { InputPropWrapper(name ?: it.name, it, this.context) }
          }
          KIND_HTML_EVENTS -> {
            (item as? VueContainer)?.emits?.mapWithNameFilter(name) { EmitCallWrapper(it, this.context) }
            ?: emptyList()
          }
          KIND_HTML_SLOTS -> {
            (item as? VueContainer)?.slots?.mapWithNameFilter(name) { SlotWrapper(it, this.context) }
            ?: emptyList()
          }
          KIND_VUE_MODEL -> {
            (item as? VueContainer)?.model?.takeIf {
              it.prop != DEFAULT_PROP || it.event != DEFAULT_EVENT
            }?.let {
              listOf(VueModelWrapper(this.context, it))
            }
            ?: emptyList()
          }
          else -> emptyList()
        }
      else emptyList()

  }

  private class InputPropWrapper(matchedName: String, property: VueInputProperty, context: WebSymbolsContainer.Context)
    : NamedSymbolWrapper<VueInputProperty>(property, matchedName, context) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_COMPONENT_PROPS

    override val jsType: JSType?
      get() = item.jsType

    override val required: Boolean
      get() = item.required

    override val attributeValue: WebSymbol.AttributeValue =
      object : WebSymbol.AttributeValue {
        override val default: String?
          get() = item.defaultValue
      }
  }

  private class EmitCallWrapper(emitCall: VueEmitCall, context: WebSymbolsContainer.Context)
    : NamedSymbolWrapper<VueEmitCall>(emitCall, context = context) {

    override val kind: SymbolKind
      get() = KIND_HTML_EVENTS

    override val jsType: JSType?
      get() = item.eventJSType
  }

  private class SlotWrapper(slot: VueSlot, context: WebSymbolsContainer.Context)
    : NamedSymbolWrapper<VueSlot>(slot, context = context) {

    override val kind: SymbolKind
      get() = KIND_HTML_SLOTS

    override val jsType: JSType?
      get() = item.scope

  }

  private class DirectiveWrapper(matchedName: String, directive: VueDirective) :
    ScopeElementWrapper<VueDirective>(matchedName, directive) {

    override val kind: SymbolKind
      get() = KIND_HTML_VUE_DIRECTIVES

    override val name: String
      get() = item.defaultName ?: matchedName

    override val source: PsiElement?
      get() = item.source

    override fun getSymbols(namespace: Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if ((namespace == null || namespace == Namespace.HTML)
          && (kind == KIND_VUE_DIRECTIVE_ARGUMENT || (name != null && kind == KIND_VUE_DIRECTIVE_MODIFIERS))) {
        listOf(AnyWrapper(this.context, Namespace.HTML, kind, name ?: "Vue directive argument"))
      }
      else emptyList()

  }

  private class VueModelWrapper(override val context: WebSymbolsContainer.Context,
                                private val vueModel: VueModelDirectiveProperties) : WebSymbol {

    override val namespace: Namespace get() = Namespace.HTML
    override val kind: SymbolKind get() = KIND_VUE_MODEL

    override val properties: Map<String, Any>
      get() = mapOf(
        Pair(PROP_VUE_MODEL_PROP, vueModel.prop),
        Pair(PROP_VUE_MODEL_EVENT, vueModel.event),
      )
  }

  private class AnyWrapper(override val context: WebSymbolsContainer.Context,
                           override val namespace: Namespace,
                           override val kind: SymbolKind,
                           override val matchedName: String) : WebSymbol {

  }

}
// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_EVENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_SLOTS
import com.intellij.javascript.web.symbols.WebSymbol.NameSegment
import com.intellij.javascript.web.symbols.WebSymbol.Priority
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Companion.NAMESPACE_HTML
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Namespace
import com.intellij.javascript.web.symbols.patterns.RegExpPattern
import com.intellij.javascript.web.symbols.patterns.WebSymbolsPattern
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_EVENT
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_PROP
import org.jetbrains.vuejs.model.source.VueUnresolvedComponent
import java.util.*

class VueWebSymbolsAdditionalContextProvider : WebSymbolsAdditionalContextProvider {

  companion object {
    const val KIND_VUE_TOP_LEVEL_ELEMENTS = "vue-file-top-elements"
    const val KIND_VUE_COMPONENTS = "vue-components"
    const val KIND_VUE_COMPONENT_PROPS = "props"
    const val KIND_VUE_DIRECTIVES = "vue-directives"
    const val KIND_VUE_AVAILABLE_SLOTS = "vue-available-slots"
    const val KIND_VUE_MODEL = "vue-model"
    const val KIND_VUE_DIRECTIVE_ARGUMENT = "argument"
    const val KIND_VUE_DIRECTIVE_MODIFIERS = "modifiers"

    const val PROP_VUE_MODEL_PROP = "prop"
    const val PROP_VUE_MODEL_EVENT = "event"

    private fun <T> List<T>.mapWithNameFilter(name: String?,
                                              params: WebSymbolsNameMatchQueryParams,
                                              context: Stack<WebSymbolsContainer>,
                                              mapper: (T) -> WebSymbol): List<WebSymbol> =
      if (name != null) {
        asSequence()
          .map(mapper)
          .flatMap { it.match(name, context, params) }
          .toList()
      }
      else this.map(mapper)

  }

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> =
    element
      ?.takeIf { framework == VueFramework.ID }
      ?.let { VueModelManager.findEnclosingContainer(it) }
      ?.let {
        listOfNotNull(EntityContainerWrapper(element, it),
          (element as? XmlTag)?.let { tag -> AvailableSlotsContainer(tag) })
      }
    ?: emptyList()

  class VueSymbolsCodeCompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {
    override fun customize(codeCompletionItem: WebSymbolCodeCompletionItem,
                           namespace: Namespace, kind: SymbolKind): WebSymbolCodeCompletionItem =
      if (namespace == Namespace.HTML)
        when (kind) {
          WebSymbol.KIND_HTML_ATTRIBUTES ->
            codeCompletionItem.symbol
              ?.takeIf { it.kind == KIND_VUE_COMPONENT_PROPS }
              ?.jsType?.getTypeText(JSType.TypeTextFormat.PRESENTABLE)
              ?.let { codeCompletionItem.withTypeText(it) }
            ?: codeCompletionItem
          else -> codeCompletionItem
        }
      else codeCompletionItem
  }

  private abstract class VueWrapperBase : WebSymbolsContainer {

    val namespace: Namespace
      get() = Namespace.HTML

  }

  private class EntityContainerWrapper(private val element: PsiElement,
                                       private val container: VueEntitiesContainer) : VueWrapperBase() {

    private val isTopLevelTag = (element as? XmlTag)?.let { tag -> tag.parentTag == null } == true
    private val containingFile: PsiFile = element.containingFile.originalFile

    override fun createPointer(): Pointer<WebSymbolsContainer> {
      val element = this.element.createSmartPointer()
      return Pointer {
        val newElement = element.dereference() ?: return@Pointer null
        val newContainer = VueModelManager.findEnclosingContainer(newElement)
        EntityContainerWrapper(newElement, newContainer)
      }

    }

    override fun hashCode(): Int = containingFile.hashCode()

    override fun equals(other: Any?): Boolean =
      other is EntityContainerWrapper
      && other.containingFile == containingFile
      && other.container == container
      && other.isTopLevelTag == isTopLevelTag

    override fun getModificationCount(): Long =
      PsiModificationTracker.SERVICE.getInstance(containingFile.project).modificationCount +
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount

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
                if (name == null) listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS)
                else listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS, name),
                context = context
              )
                .map {
                  WebSymbolMatch(it.name, it.nameSegments, Namespace.HTML, KIND_HTML_ELEMENTS, it.origin)
                }
            }
            else emptyList()
          }
          KIND_VUE_COMPONENTS -> {
            val result = mutableListOf<VueComponent>()
            if (params.registry.allowResolve) {
              val normalizedTagName = name?.let { fromAsset(it) }
              container.acceptEntities(object : VueModelProximityVisitor() {
                override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
                  return acceptSameProximity(proximity, normalizedTagName == null || fromAsset(name) == normalizedTagName) {
                    if (isNotIncorrectlySelfReferred(component)) {
                      result.add(component)
                    }
                  }
                }
              }, VueModelVisitor.Proximity.GLOBAL)
            }
            result.mapNotNull {
              ComponentWrapper(name ?: it.defaultName ?: return@mapNotNull null, it)
            }
          }
          KIND_VUE_DIRECTIVES -> {
            val directives = mutableListOf<VueDirective>()
            if (params.registry.allowResolve) {
              val searchName = name?.let { fromAsset(it) }
              container.acceptEntities(object : VueModelProximityVisitor() {
                override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
                  return acceptSameProximity(proximity, searchName == null || fromAsset(name) == searchName) {
                    directives.add(directive)
                  }
                }
              }, VueModelVisitor.Proximity.GLOBAL)
            }
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
          KIND_HTML_ELEMENTS -> {
            if (containingFile.virtualFile?.fileType == VueFileType.INSTANCE && isTopLevelTag) {
              params.registry.runCodeCompletionQuery(
                if (name == null) listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS)
                else listOf(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS, name),
                params.position, context = context
              )
            }
            else emptyList()
          }
          KIND_VUE_COMPONENTS -> {
            val result = mutableListOf<WebSymbolCodeCompletionItem>()
            if (params.registry.allowResolve) {
              val scriptLanguage = detectVueScriptLanguage(containingFile)
              container.acceptEntities(object : VueModelVisitor() {
                override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
                  // Cannot self refer without export declaration with component name
                  if (isNotIncorrectlySelfReferred(component)) {
                    // TODO replace with params.registry.getNameVariants(VUE_FRAMEWORK, Namespace.HTML, kind, name)
                    listOf(StringUtil.capitalize(toAsset(name)), fromAsset(name)).forEach {
                      result.add(createVueComponentLookup(component, it, scriptLanguage, proximity))
                    }
                  }
                  return true
                }
              }, VueModelVisitor.Proximity.OUT_OF_SCOPE)
            }
            result
          }
          KIND_VUE_DIRECTIVES -> {
            val result = mutableListOf<WebSymbolCodeCompletionItem>()
            if (params.registry.allowResolve) {
              container.acceptEntities(object : VueModelVisitor() {
                override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
                  result.add(WebSymbolCodeCompletionItem.create(fromAsset(name),
                    symbol = DirectiveWrapper(name, directive),
                    priority = priorityOf(proximity)))
                  return true
                }
              }, VueModelVisitor.Proximity.GLOBAL)
            }
            result
          }
          else -> emptyList()
        }
      else emptyList()

    // Cannot self refer without export declaration with component name or script setup
    private fun isNotIncorrectlySelfReferred(component: VueComponent) =
      (component.source as? JSImplicitElement)?.context.let { context ->
        context != containingFile
        || context.containingFile.castSafelyTo<XmlFile>()?.let { findScriptTag(it, true) } != null
      }

    private fun priorityOf(proximity: VueModelVisitor.Proximity): Priority =
      when (proximity) {
        VueModelVisitor.Proximity.LOCAL -> Priority.HIGHEST
        VueModelVisitor.Proximity.APP -> Priority.HIGH
        VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> Priority.NORMAL
        VueModelVisitor.Proximity.OUT_OF_SCOPE -> Priority.LOW
      }

    private fun createVueComponentLookup(component: VueComponent,
                                         name: String,
                                         scriptLanguage: String?,
                                         proximity: VueModelVisitor.Proximity): WebSymbolCodeCompletionItem {
      val element = component.source
      val wrapper = ComponentWrapper(name, component)
      var builder = WebSymbolCodeCompletionItem.create(
        name = name,
        symbol = wrapper,
        priority = priorityOf(proximity))

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

    override fun hashCode(): Int = tag.hashCode()

    override fun equals(other: Any?): Boolean =
      other is AvailableSlotsContainer
      && other.tag == tag

    override fun getModificationCount(): Long = tag.containingFile.modificationStamp

    override fun getSymbols(namespace: Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if ((namespace == null || namespace == Namespace.HTML)
          && kind == KIND_VUE_AVAILABLE_SLOTS
          && params.registry.allowResolve)
        getAvailableSlots(tag, name, true)
      else emptyList()

    override fun createPointer(): Pointer<AvailableSlotsContainer> {
      val tag = this.tag.createSmartPointer()
      return Pointer {
        tag.dereference()?.let { AvailableSlotsContainer(it) }
      }
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

  private abstract class NamedSymbolWrapper<T : VueNamedSymbol>(item: T,
                                                                protected val owner: VueComponent,
                                                                override val origin: WebSymbolsContainer.Origin)
    : DocumentedItemWrapper<T>(item.name, item) {

    override val name: String
      get() = item.name

    override val source: PsiElement?
      get() = item.source

    abstract override fun createPointer(): Pointer<NamedSymbolWrapper<T>>

    abstract class NamedSymbolPointer<T : VueNamedSymbol>(wrapper: NamedSymbolWrapper<T>)
      : Pointer<NamedSymbolWrapper<T>> {
      val name = wrapper.item.name
      val origin = wrapper.origin
      private val owner = wrapper.owner.createPointer()

      override fun dereference(): NamedSymbolWrapper<T>? =
        owner.dereference()?.let { component ->
          locateSymbol(component)
            ?.let { createWrapper(component, it) }
        }

      abstract fun locateSymbol(owner: VueComponent): T?

      abstract fun createWrapper(owner: VueComponent, symbol: T): NamedSymbolWrapper<T>

    }

  }


  private abstract class ScopeElementWrapper<T : VueDocumentedItem>(matchedName: String, item: T) :
    DocumentedItemWrapper<T>(matchedName, item) {

    override val origin: WebSymbolsContainer.Origin =
      object : WebSymbolsContainer.Origin {

        override val framework: FrameworkId
          get() = VueFramework.ID

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
      get() = KIND_VUE_COMPONENTS

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
          KIND_VUE_COMPONENT_PROPS -> {
            val props = mutableListOf<VueInputProperty>()
            // TODO ambiguous resolution in case of duplicated names
            item.acceptPropertiesAndMethods(object : VueModelVisitor() {
              override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
                props.add(prop)
                return true
              }
            })
            props.mapWithNameFilter(name, params, context) { InputPropWrapper(it, item, this.origin) }
          }
          KIND_HTML_EVENTS -> {
            (item as? VueContainer)
              ?.emits
              ?.mapWithNameFilter(name, params, context) { EmitCallWrapper(it, item, this.origin) }
            ?: emptyList()
          }
          KIND_HTML_SLOTS -> {
            (item as? VueContainer)
              ?.slots
              ?.mapWithNameFilter(name, params, context) { SlotWrapper(it, item, this.origin) }
            ?: if (!name.isNullOrEmpty()
                   && ((item is VueContainer && item.template == null)
                       || item is VueUnresolvedComponent)) {
              listOf(WebSymbolMatch(name, listOf(NameSegment(0, name.length)), Namespace.HTML, KIND_HTML_SLOTS, this.origin))
            }
            else emptyList()
          }
          KIND_VUE_MODEL -> {
            (item as? VueContainer)?.model?.takeIf {
              it.prop != DEFAULT_PROP || it.event != DEFAULT_EVENT
            }?.let {
              listOf(VueModelWrapper(this.origin, it))
            }
            ?: emptyList()
          }
          else -> emptyList()
        }
      else emptyList()

    override fun createPointer(): Pointer<ComponentWrapper> {
      val component = item.createPointer()
      val matchedName = this.matchedName
      return Pointer {
        component.dereference()?.let { ComponentWrapper(matchedName, it) }
      }
    }
  }

  private class InputPropWrapper(property: VueInputProperty,
                                 owner: VueComponent,
                                 origin: WebSymbolsContainer.Origin)
    : NamedSymbolWrapper<VueInputProperty>(property, owner, origin) {

    override val kind: SymbolKind
      get() = KIND_VUE_COMPONENT_PROPS

    override val jsType: JSType?
      get() = item.jsType

    override val required: Boolean
      get() = item.required

    override val attributeValue: WebSymbol.AttributeValue =
      object : WebSymbol.AttributeValue {
        override val default: String?
          get() = item.defaultValue
      }

    override fun createPointer(): Pointer<NamedSymbolWrapper<VueInputProperty>> =
      object : NamedSymbolPointer<VueInputProperty>(this) {

        override fun locateSymbol(owner: VueComponent): VueInputProperty? {
          var result: VueInputProperty? = null
          // TODO ambiguous resolution in case of duplicated names
          owner.acceptPropertiesAndMethods(object : VueModelVisitor() {
            override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
              if (prop.name == name) {
                result = prop
              }
              return result == null
            }
          })
          return result
        }

        override fun createWrapper(owner: VueComponent, symbol: VueInputProperty): NamedSymbolWrapper<VueInputProperty> =
          InputPropWrapper(symbol, owner, origin)

      }
  }

  private class EmitCallWrapper(emitCall: VueEmitCall,
                                owner: VueComponent,
                                origin: WebSymbolsContainer.Origin)
    : NamedSymbolWrapper<VueEmitCall>(emitCall, origin = origin, owner = owner) {

    override val kind: SymbolKind
      get() = KIND_HTML_EVENTS

    override val jsType: JSType?
      get() = item.eventJSType

    override fun createPointer(): Pointer<NamedSymbolWrapper<VueEmitCall>> =
      object : NamedSymbolPointer<VueEmitCall>(this) {

        override fun locateSymbol(owner: VueComponent): VueEmitCall? =
          (owner as? VueContainer)?.emits?.find { it.name == name }

        override fun createWrapper(owner: VueComponent, symbol: VueEmitCall): NamedSymbolWrapper<VueEmitCall> =
          EmitCallWrapper(symbol, owner, origin)

      }
  }

  private class SlotWrapper(slot: VueSlot,
                            owner: VueComponent,
                            origin: WebSymbolsContainer.Origin)
    : NamedSymbolWrapper<VueSlot>(slot, origin = origin, owner = owner) {

    override val pattern: WebSymbolsPattern?
      get() = item.pattern?.let { RegExpPattern(it, true) }

    override val kind: SymbolKind
      get() = KIND_HTML_SLOTS

    override val jsType: JSType?
      get() = item.scope

    override fun createPointer(): Pointer<NamedSymbolWrapper<VueSlot>> =
      object : NamedSymbolPointer<VueSlot>(this) {

        override fun locateSymbol(owner: VueComponent): VueSlot? =
          (owner as? VueContainer)?.slots?.find { it.name == name }

        override fun createWrapper(owner: VueComponent, symbol: VueSlot): NamedSymbolWrapper<VueSlot> =
          SlotWrapper(symbol, owner, origin)

      }

  }

  private class DirectiveWrapper(matchedName: String, directive: VueDirective) :
    ScopeElementWrapper<VueDirective>(matchedName, directive) {

    override val kind: SymbolKind
      get() = KIND_VUE_DIRECTIVES

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
        listOf(AnyWrapper(this.origin, Namespace.HTML, kind, name ?: "Vue directive argument"))
      }
      else emptyList()

    override fun createPointer(): Pointer<DirectiveWrapper> {
      val component = item.createPointer()
      val matchedName = this.matchedName
      return Pointer {
        component.dereference()?.let { DirectiveWrapper(matchedName, it) }
      }
    }
  }

  private class VueModelWrapper(override val origin: WebSymbolsContainer.Origin,
                                private val vueModel: VueModelDirectiveProperties) : WebSymbol {

    override val namespace: Namespace get() = Namespace.HTML
    override val kind: SymbolKind get() = KIND_VUE_MODEL

    override val properties: Map<String, Any>
      get() = mapOf(
        Pair(PROP_VUE_MODEL_PROP, vueModel.prop),
        Pair(PROP_VUE_MODEL_EVENT, vueModel.event),
      )

    override fun createPointer(): Pointer<VueModelWrapper> =
      Pointer.hardPointer(this)
  }

  private class AnyWrapper(override val origin: WebSymbolsContainer.Origin,
                           override val namespace: Namespace,
                           override val kind: SymbolKind,
                           override val matchedName: String) : WebSymbol {

    override val pattern: WebSymbolsPattern
      get() = RegExpPattern(".*", false)

    override fun createPointer(): Pointer<AnyWrapper> =
      Pointer.hardPointer(this)
  }

}
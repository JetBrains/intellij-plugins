// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.codeInsight.navigation.targetPresentation
import com.intellij.ide.util.EditSourceUtil
import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_SLOTS
import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_JS_EVENTS
import com.intellij.javascript.web.symbols.WebSymbol.NameSegment
import com.intellij.javascript.web.symbols.WebSymbol.Priority
import com.intellij.javascript.web.symbols.WebSymbolCodeCompletionItemCustomizer.Companion.decorateWithSymbolType
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Companion.NAMESPACE_HTML
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Namespace
import com.intellij.javascript.web.symbols.patterns.RegExpPattern
import com.intellij.javascript.web.symbols.patterns.WebSymbolsPattern
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.navigation.EmptyNavigatable
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.SmartList
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponents
import org.jetbrains.vuejs.model.source.VueUnresolvedComponent
import org.jetbrains.vuejs.model.typed.VueTypedComponent
import org.jetbrains.vuejs.model.typed.VueTypedEntitiesProvider
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

    const val PROP_VUE_PROXIMITY = "x-vue-proximity"

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

    private fun priorityOf(proximity: VueModelVisitor.Proximity): Priority =
      when (proximity) {
        VueModelVisitor.Proximity.LOCAL -> Priority.HIGHEST
        VueModelVisitor.Proximity.APP -> Priority.HIGH
        VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> Priority.NORMAL
        VueModelVisitor.Proximity.OUT_OF_SCOPE -> Priority.LOW
      }
  }

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> {
    if (framework != VueFramework.ID || element == null) return emptyList()
    val result = SmartList<WebSymbolsContainer>()
    val tag = (element as? XmlAttribute)?.parent ?: element as? XmlTag
    val fileContext = element.containingFile.originalFile

    // Entity containers
    VueModelManager.findEnclosingContainer(element).let { enclosingContainer ->
      val containers = mutableMapOf<VueEntitiesContainer, VueModelVisitor.Proximity>()

      containers[enclosingContainer] = VueModelVisitor.Proximity.LOCAL

      enclosingContainer.parents.forEach { parent ->
        when (parent) {
          is VueApp -> containers[parent] = VueModelVisitor.Proximity.APP
          is VuePlugin -> containers[parent] = VueModelVisitor.Proximity.PLUGIN
        }
      }

      enclosingContainer.global?.let { global ->
        val apps = containers.keys.filterIsInstance<VueApp>()
        global.plugins.forEach { plugin ->
          containers.computeIfAbsent(plugin) {
            apps.maxOfOrNull { it.getProximity(plugin) } ?: plugin.defaultProximity
          }
        }
        containers[global] = VueModelVisitor.Proximity.GLOBAL
      }

      containers.forEach { (container, proximity) ->
        EntityContainerWrapper.create(container, proximity)
          ?.let {
            if (container == enclosingContainer || container is VueGlobal) {
              IncorrectlySelfReferredComponentFilteringContainer(it, fileContext)
            }
            else it
          }
          ?.let {
            result.add(it)
          }
      }
    }

    // Slots container
    tag?.let { result.add(AvailableSlotsContainer(it)) }

    // Top level tags
    if (tag != null && tag.parentTag == null && fileContext.virtualFile?.fileType == VueFileType.INSTANCE) {
      result.add(TopLevelElementsContainer)
    }

    return result
  }

  class VueSymbolsCodeCompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {
    override fun customize(item: WebSymbolCodeCompletionItem,
                           framework: FrameworkId?, namespace: Namespace, kind: SymbolKind): WebSymbolCodeCompletionItem =
      if (namespace == Namespace.HTML && framework == VueFramework.ID)
        when (kind) {
          WebSymbol.KIND_HTML_ATTRIBUTES ->
            item.symbol
              ?.takeIf { it.kind == KIND_VUE_COMPONENT_PROPS || it.kind == KIND_JS_EVENTS }
              ?.let { item.decorateWithSymbolType(it) }
            ?: item
          else -> item
        }
      else item
  }

  private abstract class VueWrapperBase : WebSymbolsContainer {

    open val namespace: Namespace
      get() = Namespace.HTML

  }

  private object TopLevelElementsContainer : WebSymbolsContainer {

    override fun getSymbols(namespace: Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      if (namespace == Namespace.HTML && kind == KIND_HTML_ELEMENTS)
        params.registry.runNameMatchQuery(
          listOfNotNull(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS, name),
          context = context,
          virtualSymbols = params.virtualSymbols,
          strictScope = params.strictScope,
          abstractSymbols = params.abstractSymbols,
        )
          .map {
            WebSymbolMatch.create(it.name, it.nameSegments, Namespace.HTML, KIND_HTML_ELEMENTS, it.origin)
          }
      else emptyList()

    override fun getCodeCompletions(namespace: Namespace?,
                                    kind: SymbolKind,
                                    name: String?,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
      if (namespace == Namespace.HTML && kind == KIND_HTML_ELEMENTS)
        params.registry.runCodeCompletionQuery(
          listOfNotNull(NAMESPACE_HTML, KIND_VUE_TOP_LEVEL_ELEMENTS, name),
          context = context,
          position = params.position,
          virtualSymbols = params.virtualSymbols,
        )
      else emptyList()

    override fun createPointer(): Pointer<out WebSymbolsContainer> = Pointer.hardPointer(this)

    override fun getModificationCount(): Long = 0

  }

  private class EntityContainerWrapper<K> private constructor(private val container: VueEntitiesContainer,
                                                              project: Project,
                                                              dataHolder: UserDataHolder,
                                                              private val proximity: VueModelVisitor.Proximity,
                                                              key: K)
    : WebSymbolsContainerWithCache<UserDataHolder, K>(VueFramework.ID, project, dataHolder, key) {

    companion object {
      fun create(container: VueEntitiesContainer, proximity: VueModelVisitor.Proximity): EntityContainerWrapper<*>? {
        container.source
          ?.let {
            return EntityContainerWrapper(container, it.project, it, proximity, proximity)
          }
        return if (container is VueGlobal)
          EntityContainerWrapper(container, container.project, container.project, proximity, container.packageJsonUrl ?: "")
        else null
      }
    }

    override fun createPointer(): Pointer<EntityContainerWrapper<K>> {
      val containerPtr = container.createPointer()
      val dataHolderPtr = dataHolder.let { if (it is Project) Pointer.hardPointer(it) else (it as PsiElement).createSmartPointer() }
      val project = this.project
      val proximity = this.proximity
      val key = this.key
      return Pointer {
        val container = containerPtr.dereference() ?: return@Pointer null
        val dataHolder = dataHolderPtr.dereference() ?: return@Pointer null
        EntityContainerWrapper(container, project, dataHolder, proximity, key)
      }
    }

    override fun getModificationCount(): Long =
      PsiModificationTracker.SERVICE.getInstance(project).modificationCount +
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount

    override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      visitContainer(container, proximity, consumer)
      if (container is VueGlobal) {
        visitContainer(container.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE, consumer)
      }
      cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    }

    private fun visitContainer(container: VueEntitiesContainer, forcedProximity: VueModelVisitor.Proximity, consumer: (WebSymbol) -> Unit) {
      container.acceptEntities(object : VueModelVisitor() {

        override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
          consumer(ComponentWrapper(toAsset(name, true), component, forcedProximity))
          return true
        }

        override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
          consumer(DirectiveWrapper(name, directive, forcedProximity))
          return true
        }

      }, VueModelVisitor.Proximity.LOCAL)
    }
  }

  private class IncorrectlySelfReferredComponentFilteringContainer(private val delegate: WebSymbolsContainer,
                                                                   private val file: PsiFile) : WebSymbolsContainer {

    override fun getSymbols(namespace: Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
      delegate.getSymbols(namespace, kind, name, params, context)
        .filter { isNotIncorrectlySelfReferred(it) }

    override fun getCodeCompletions(namespace: Namespace?,
                                    kind: SymbolKind,
                                    name: String?,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
      delegate.getCodeCompletions(namespace, kind, name, params, context)
        .filter { isNotIncorrectlySelfReferred(it.symbol) }

    override fun createPointer(): Pointer<out WebSymbolsContainer> {
      val delegatePtr = delegate.createPointer()
      val filePtr = file.createSmartPointer()
      return Pointer {
        val delegate = delegatePtr.dereference() ?: return@Pointer null
        val file = filePtr.dereference() ?: return@Pointer null
        IncorrectlySelfReferredComponentFilteringContainer(delegate, file)
      }
    }

    override fun getModificationCount(): Long =
      delegate.modificationCount

    override fun equals(other: Any?): Boolean =
      other is IncorrectlySelfReferredComponentFilteringContainer
      && other.delegate == delegate
      && other.file == file

    override fun hashCode(): Int =
      Objects.hash(delegate, file)

    // Cannot self refer without export declaration with component name or script setup
    private fun isNotIncorrectlySelfReferred(symbol: WebSymbolsContainer?) =
      symbol !is PsiSourcedWebSymbol
      || (symbol.source as? JSImplicitElement)?.context.let { context ->
        context == null
        || context != file
        || context.containingFile.castSafelyTo<XmlFile>()?.let { findScriptTag(it, true) } != null
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
    override val matchedName: String, protected val item: T) : VueWrapperBase(), PsiSourcedWebSymbol {

    override val description: String?
      get() = item.documentation.description

    override val docUrl: String?
      get() = item.documentation.docUrl

    override fun equals(other: Any?): Boolean =
      other is DocumentedItemWrapper<*>
      && other.javaClass == this.javaClass
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

  private class ComponentWrapper(matchedName: String, component: VueComponent, val vueProximity: VueModelVisitor.Proximity) :
    ScopeElementWrapper<VueComponent>(matchedName, component) {

    override val kind: SymbolKind
      get() = KIND_VUE_COMPONENTS

    override val name: String
      get() = matchedName

    override val source: PsiElement?
      get() = item.source

    override val priority: Priority
      get() = priorityOf(vueProximity)

    override fun equals(other: Any?): Boolean =
      super.equals(other)
      && (other as ComponentWrapper).vueProximity == vueProximity

    override fun hashCode(): Int =
      31 * super.hashCode() + vueProximity.hashCode()

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      source?.let { listOf(ComponentSourceNavigationTarget(it)) } ?: emptyList()

    override val properties: Map<String, Any>
      get() = mapOf(Pair(PROP_VUE_PROXIMITY, vueProximity))

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
          KIND_HTML_SLOTS -> {
            (item as? VueContainer)
              ?.slots
              ?.mapWithNameFilter(name, params, context) { SlotWrapper(it, item, this.origin) }
            ?: if (!name.isNullOrEmpty()
                   && ((item is VueContainer && item.template == null)
                       || item is VueUnresolvedComponent)) {
              listOf(WebSymbolMatch.create(name, listOf(NameSegment(0, name.length)), Namespace.HTML, KIND_HTML_SLOTS, this.origin))
            }
            else emptyList()
          }
          KIND_VUE_MODEL -> {
            (item as? VueContainer)
              ?.collectModelDirectiveProperties()
              ?.takeIf { it.prop != null || it.event != null }
              ?.let { listOf(VueModelWrapper(this.origin, it)) }
            ?: emptyList()
          }
          else -> emptyList()
        }
      else if (namespace == Namespace.JS && kind == KIND_JS_EVENTS) {
        (item as? VueContainer)
          ?.emits
          ?.mapWithNameFilter(name, params, context) { EmitCallWrapper(it, item, this.origin) }
        ?: emptyList()
      }
      else emptyList()

    override fun createPointer(): Pointer<ComponentWrapper> {
      val component = item.createPointer()
      val matchedName = this.matchedName
      val vueProximity = this.vueProximity
      return Pointer {
        component.dereference()?.let { ComponentWrapper(matchedName, it, vueProximity) }
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

    override val namespace: Namespace
      get() = Namespace.JS

    override val kind: SymbolKind
      get() = KIND_JS_EVENTS

    override val jsType: JSType?
      get() = item.eventJSType

    override val priority: Priority
      get() = Priority.HIGHEST

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

  private class DirectiveWrapper(matchedName: String, directive: VueDirective, val vueProximity: VueModelVisitor.Proximity) :
    ScopeElementWrapper<VueDirective>(fromAsset(matchedName), directive) {

    override val kind: SymbolKind
      get() = KIND_VUE_DIRECTIVES

    override val name: String
      get() = matchedName

    override val source: PsiElement?
      get() = item.source

    override val priority: Priority
      get() = priorityOf(vueProximity)

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
      val vueProximity = this.vueProximity
      return Pointer {
        component.dereference()?.let { DirectiveWrapper(matchedName, it, vueProximity) }
      }
    }
  }

  private class VueModelWrapper(override val origin: WebSymbolsContainer.Origin,
                                private val vueModel: VueModelDirectiveProperties) : WebSymbol {

    override val namespace: Namespace get() = Namespace.HTML
    override val kind: SymbolKind get() = KIND_VUE_MODEL

    override val properties: Map<String, Any>
      get() {
        val map = mutableMapOf<String, Any>()
        vueModel.prop?.let { map[PROP_VUE_MODEL_PROP] = it }
        vueModel.event?.let { map[PROP_VUE_MODEL_EVENT] = it }
        return map
      }

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

  private class ComponentSourceNavigationTarget(private val myElement: PsiElement) : NavigationTarget {

    override fun isValid(): Boolean = myElement.isValid

    override fun getNavigatable(): Navigatable =
      (VueComponents.getComponentDescriptor(myElement)?.source ?: myElement).let {
        it as? Navigatable
        ?: EditSourceUtil.getDescriptor(it)
        ?: EmptyNavigatable.INSTANCE
      }

    override fun getTargetPresentation(): TargetPresentation = targetPresentation(myElement)

    override fun equals(other: Any?): Boolean =
      this === other ||
      other is ComponentSourceNavigationTarget
      && other.myElement == myElement

    override fun hashCode(): Int =
      myElement.hashCode()
  }

}
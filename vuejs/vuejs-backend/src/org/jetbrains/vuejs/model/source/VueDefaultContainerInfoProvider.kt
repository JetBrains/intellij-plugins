// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSFunctionProperty
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.JSComputedPropertyNameOwner
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.JSWidenType
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.SETUP_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.VAPOR_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.getDefaultTypeFromPropOptions
import org.jetbrains.vuejs.codeInsight.getPropOptionality
import org.jetbrains.vuejs.codeInsight.getRequiredFromPropOptions
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.VUE_EXTENDS_BINDING_INDEX_JS_KEY
import org.jetbrains.vuejs.index.VUE_EXTENDS_BINDING_INDEX_KEY
import org.jetbrains.vuejs.index.VUE_MIXIN_BINDING_INDEX_KEY
import org.jetbrains.vuejs.index.hasAttribute
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEmitCall
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueInject
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueMixin
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.model.VuePsiSourcedComponent
import org.jetbrains.vuejs.model.VueSlot
import org.jetbrains.vuejs.model.getSlotsTypeFromTypedProperty
import org.jetbrains.vuejs.model.isInjectionSymbolType
import org.jetbrains.vuejs.model.resolveInjectionSymbol
import org.jetbrains.vuejs.model.source.VueComponents.getComponent
import org.jetbrains.vuejs.model.typed.VueTypedMixin
import org.jetbrains.vuejs.types.VueSourcePropType
import org.jetbrains.vuejs.types.optionalIf

class VueDefaultContainerInfoProvider : VueContainerInfoProvider.VueInitializedContainerInfoProvider(::VueSourceContainerInfo) {

  private class VueSourceContainerInfo(declaration: JSElement) : VueInitializedContainerInfo(declaration) {
    override val data: List<VueDataProperty> get() = get(Holder.DATA)
    override val computed: List<VueComputedProperty> get() = get(Holder.COMPUTED)
    override val methods: List<VueMethod> get() = get(Holder.METHODS)
    override val props: List<VueInputProperty> get() = get(Holder.PROPS)
    override val emits: List<VueEmitCall> get() = get(Holder.EMITS)
    override val slots: List<VueSlot> get() = get(Holder.SLOTS)
    override val model: VueModelDirectiveProperties get() = get(Holder.MODEL)

    override val delimiters: Pair<String, String>? get() = get(Holder.DELIMITERS).get()
    override val extends: List<VueMixin> get() = get(Holder.EXTENDS) + get(Holder.EXTENDS_CALL)
    override val components: Map<String, VueNamedComponent> get() = get(Holder.COMPONENTS)
    override val directives: Map<String, VueDirective> get() = get(Holder.DIRECTIVES)
    override val mixins: List<VueMixin> get() = get(Holder.MIXINS)
    override val filters: Map<String, VueFilter> get() = get(Holder.FILTERS)
    override val provides: List<VueProvide> get() = get(Holder.PROVIDES)
    override val injects: List<VueInject> get() = get(Holder.INJECTS)

  }

  private object Holder {
    object ContainerMember {
      val Props: MemberReader = MemberReader(PROPS_PROP, true)
      val Computed = MemberReader(COMPUTED_PROP)
      val Methods = MemberReader(METHODS_PROP)
      val Emits = MemberReader(EMITS_PROP, true, false)
      val Slots = MemberReader(SLOTS_PROP, customTypeProvider = ::getSlotsTypeFromTypedProperty)
      val Directives = MemberReader(DIRECTIVES_PROP)
      val Components = MemberReader(COMPONENTS_PROP)
      val Filters = MemberReader(FILTERS_PROP)
      val Delimiters = MemberReader(DELIMITERS_PROP, true, false)
      val Model = MemberReader(MODEL_PROP)
      val Data = MemberReader(DATA_PROP, canBeFunctionResult = true)
      val Provides = MemberReader(PROVIDE_PROP, canBeFunctionResult = true, includeComputed = true)
      val Injects = MemberReader(INJECT_PROP, canBeArray = true)
    }

    val EXTENDS = MixinsAccessor(EXTENDS_PROP, VUE_EXTENDS_BINDING_INDEX_KEY)
    val EXTENDS_CALL = ExtendsCallAccessor()
    val MIXINS = MixinsAccessor(MIXINS_PROP, VUE_MIXIN_BINDING_INDEX_KEY)
    val DIRECTIVES = DirectivesAccessor()
    val COMPONENTS = ComponentsAccessor()
    val FILTERS = SimpleMemberMapAccessor(ContainerMember.Filters, ::VueSourceFilter)
    val DELIMITERS = DelimitersAccessor()
    val PROVIDES = ProvidesAccessor()
    val INJECTS = SimpleMemberAccessor(ContainerMember.Injects, ::VueSourceInject)
    val PROPS = SimpleMemberAccessor(ContainerMember.Props, VueSourceInputProperty.Companion::create)
    val DATA = SimpleMemberAccessor(ContainerMember.Data, ::VueSourceDataProperty)
    val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VueSourceComputedProperty)
    val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VueSourceMethod)
    val EMITS = SimpleMemberAccessor(ContainerMember.Emits, ::VueSourceEmitDefinition)
    val SLOTS = SimpleMemberAccessor(ContainerMember.Slots, ::VueSourceSlot)
    val MODEL = ModelAccessor()
  }

  private class ExtendsCallAccessor : ListAccessor<VueMixin>() {
    override fun build(declaration: JSElement): List<VueMixin> =
      declaration.context
        ?.let { if (it is JSArgumentList) it.context else it }
        ?.asSafely<JSCallExpression>()
        ?.indexingData
        ?.implicitElements
        ?.asSequence()
        ?.filter { it.userString == VUE_EXTENDS_BINDING_INDEX_JS_KEY }
        ?.mapNotNull { VueComponents.vueMixinFinder(it) }
        ?.toList()
      ?: emptyList()
  }

  private class MixinsAccessor(
    private val propertyName: String,
    private val indexKey: StubIndexKey<String, JSImplicitElementProvider>,
  ) : ListAccessor<VueMixin>() {

    override fun build(declaration: JSElement): List<VueMixin> {
      val mixinsProperty = declaration.asSafely<JSObjectLiteralExpression>()
                             ?.findProperty(propertyName) ?: return emptyList()
      val original = CompletionUtil.getOriginalOrSelf<PsiElement>(mixinsProperty)
      val referencedMixins: List<VueMixin> =
        resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile.originalFile), indexKey)
          .asSequence()
          .filter { PsiTreeUtil.isAncestor(original, it.parent, false) }
          .mapNotNull { VueComponents.vueMixinFinder(it) }
          .toList()

      val expressions: List<JSExpression> =
        (mixinsProperty as? StubBasedPsiElement<*>)?.stub
          ?.let { stub ->
            stub.childrenStubs.mapNotNull { it.psi as? JSExpression }
          }
        ?: (mixinsProperty.value as? JSArrayLiteralExpression)
          ?.expressions
          ?.toList()
        ?: emptyList()

      val initializerMixins: List<VueMixin> = expressions
        .mapNotNull {
          when (it) {
            is JSCallExpression -> VueTypedMixin(it)
            is JSObjectLiteralExpression -> VueModelManager.getMixin(it)
            else -> null
          }
        }

      return referencedMixins + initializerMixins
    }
  }

  private class DirectivesAccessor : MapAccessor<VueDirective>() {
    override fun build(declaration: JSElement): Map<String, VueDirective> {
      return StreamEx.of(Holder.ContainerMember.Directives.readMembers(declaration))
        .mapToEntry({ it.first }, {
          (VueComponents.meaningfulExpression(it.second) ?: it.second)
            .let { meaningfulElement ->
              objectLiteralFor(meaningfulElement)
              ?: meaningfulElement
            }.let { initializer ->
              @Suppress("USELESS_CAST")
              VueSourceDirective(it.first, initializer) as VueDirective
            }
        })
        .distinctKeys()
        .into(mutableMapOf<String, VueDirective>())
    }
  }

  private class ComponentsAccessor : MapAccessor<VueNamedComponent>() {
    override fun build(declaration: JSElement): Map<String, VueNamedComponent> {
      return Holder.ContainerMember.Components.readMembers(declaration)
        .asSequence()
        .mapNotNull { (_, element) ->
          val component = when (val meaningfulElement = VueComponents.meaningfulExpression(element) ?: element) {
            is ES6ImportedBinding ->
              meaningfulElement.declaration?.fromClause
                ?.resolveReferencedElements()
                ?.find { it is JSEmbeddedContent }
                ?.context
                ?.asSafely<XmlTag>()
                ?.takeIf {
                  hasAttribute(it, SETUP_ATTRIBUTE_NAME)
                  || hasAttribute(it, VAPOR_ATTRIBUTE_NAME)
                }
                ?.containingFile
                ?.let { getComponent(it) }
            is HtmlFileImpl -> getComponent(meaningfulElement)
            else -> getComponent(meaningfulElement as? JSElement)
          } ?: VueUnresolvedComponent(element)
            if (element is JSPsiNamedElementBase && !(component is VuePsiSourcedComponent && component.source == element))
              VueLocallyDefinedComponent.create(component, element)
            else
              component as? VueNamedComponent
        }
        .associateBy { it.name }
    }
  }

  private class ProvidesAccessor : ListAccessor<VueProvide>() {
    override fun build(declaration: JSElement): List<VueProvide> {
      return Holder.ContainerMember.Provides.readMembers(declaration).mapNotNull { (name, element) ->
        if (element is JSComputedPropertyNameOwner && element.computedPropertyName != null) {
          JSStubBasedPsiTreeUtil.resolveLocally(name, element).asSafely<PsiNamedElement>()
            ?.let { VueSourceProvide(name, element, it) }
        }
        else {
          VueSourceProvide(name, element)
        }
      }
    }
  }

  private class ModelAccessor : MemberAccessor<VueModelDirectiveProperties>() {
    override fun build(declaration: JSElement): VueModelDirectiveProperties {
      var prop: String? = null
      var event: String? = null
      Holder.ContainerMember.Model.readMembers(declaration).forEach { (name, element) ->
        (element as? JSProperty)?.value
          ?.let { getTextIfLiteral(it) }
          ?.let { value ->
            if (name == MODEL_PROP_PROP)
              prop = value
            else if (name == MODEL_EVENT_PROP)
              event = value
          }
      }
      return VueModelDirectiveProperties(prop, event)
    }
  }

  private class DelimitersAccessor : MemberAccessor<Ref<Pair<String, String>>>() {
    override fun build(declaration: JSElement): Ref<Pair<String, String>> {
      val delimiters = Holder.ContainerMember.Delimiters.readMembers(declaration)
      if (delimiters.size == 2
          && delimiters[0].first.isNotBlank()
          && delimiters[1].first.isNotBlank()) {
        return Ref(Pair(delimiters[0].first, delimiters[1].first))
      }
      return Ref(null)
    }
  }


  abstract class VueSourceInputProperty<T : PsiElement> protected constructor(
    override val name: String,
    val sourceElement: T,
    protected val hasOuterDefault: Boolean = false,
  ) : VueInputProperty {

    override val required: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
      // script setup defineProps runtime declarations rely on this class (see VueScriptSetupInfoProvider)
      // withDefaults call is incompatible, but defaults from props destructure should work
      !hasOuterDefault && getRequiredFromPropOptions((sourceElement as? JSProperty)?.initializerOrStub)
    }

    override val optional: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
      getPropOptionality((sourceElement as? JSProperty)?.initializerOrStub, required)
    }

    override val type: JSType?
      get() = (sourceElement as? PsiNamedElement)
        ?.let { VueSourcePropType(it) }
        ?.optionalIf(optional)

    override fun toString(): String {
      return "VueSourceInputProperty(name='$name', required=$required, jsType=$type)"
    }

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is VueSourceInputProperty<*>
      && other.sourceElement == sourceElement

    override fun hashCode(): Int =
      sourceElement.hashCode()

    companion object {
      fun create(
        name: String,
        sourceElement: PsiElement,
        hasOuterDefault: Boolean = false,
      ): VueSourceInputProperty<*>? =
        when (sourceElement) {
          is JSLiteralExpression if sourceElement.isQuotedLiteral ->
            VueStringLiteralInputProperty(name, sourceElement, hasOuterDefault)
          is JSProperty ->
            VuePsiNamedElementInputProperty(name, sourceElement, hasOuterDefault)
          is JSImplicitElement if sourceElement.context is JSProperty ->
            VuePsiNamedElementInputProperty(name, sourceElement, hasOuterDefault)
          else -> null
        }
    }

  }

  private class VuePsiNamedElementInputProperty(
    name: String,
    sourceElement: PsiNamedElement,
    hasOuterDefault: Boolean,
  ) : VueSourceInputProperty<PsiNamedElement>(name, sourceElement, hasOuterDefault),
      PsiSourcedPolySymbol {

    override val source: PsiNamedElement get() = sourceElement

    override fun createPointer(): Pointer<out VuePsiNamedElementInputProperty> {
      val name = name
      val propertyPtr = sourceElement.createSmartPointer()
      val hasOuterDefault = hasOuterDefault
      return Pointer {
        val property = propertyPtr.dereference() ?: return@Pointer null
        VuePsiNamedElementInputProperty(property.name ?: name, property, hasOuterDefault)
      }
    }

  }

  private class VueStringLiteralInputProperty(
    name: String,
    sourceElement: JSLiteralExpression,
    hasOuterDefault: Boolean,
  ) : VueSourceInputProperty<JSLiteralExpression>(name, sourceElement, hasOuterDefault),
      PolySymbolDeclaredInPsi {

    override val textRangeInSourceElement: TextRange
      get() = TextRange(1, sourceElement.textRange.length - 1)

    override val psiContext: PsiElement?
      get() = super<PolySymbolDeclaredInPsi>.psiContext

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<PolySymbolDeclaredInPsi>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueStringLiteralInputProperty> {
      val name = name
      val sourceElementPtr = sourceElement.createSmartPointer()
      val hasOuterDefault = hasOuterDefault
      return Pointer {
        val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
        VueStringLiteralInputProperty(sourceElement.stringValue ?: name, sourceElement, hasOuterDefault)
      }
    }

  }

  private abstract class VueSourceProperty(
    override val name: String,
    private val originalSource: PsiElement,
  ) : VueProperty, PsiSourcedPolySymbol {

    override val source: PsiElement = originalSource

    abstract override fun createPointer(): Pointer<out VueSourceProperty>

    protected fun <T : VueSourceProperty> createPointer(constructor: (String, PsiElement) -> T): Pointer<T> {
      val name = name
      val elementPtr = originalSource.createSmartPointer()
      return Pointer {
        elementPtr.dereference()?.let { constructor(name, originalSource) }
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this
      || other is VueSourceProperty
      && other.javaClass == javaClass
      && other.name == name
      && other.originalSource == originalSource

    override fun hashCode(): Int {
      var result = name.hashCode()
      result = 31 * result + originalSource.hashCode()
      return result
    }

  }

  private class VueSourceDataProperty(
    name: String,
    source: PsiElement,
  ) : VueSourceProperty(name, source), VueDataProperty {

    override val type: JSType?
      get() = when (source) {
        is JSProperty -> JSResolveUtil.getElementJSType(source)
        is JSImplicitElement -> source.jsType
        else -> null
      }

    override fun createPointer(): Pointer<VueSourceDataProperty> =
      createPointer(::VueSourceDataProperty)
  }

  private class VueSourceComputedProperty(
    name: String,
    sourceElement: PsiElement,
  ) : VueSourceProperty(name, sourceElement), VueComputedProperty {

    override val source: VueImplicitElement
    override val type: JSType?

    init {
      var provider = sourceElement
      val returnType = when (sourceElement) {
        is JSImplicitElement -> {
          provider = sourceElement.context ?: sourceElement
          sourceElement.jsType?.let {
            JSApplyCallType(it, JSTypeSourceFactory.createTypeSource(sourceElement, false))
          }
        }
        is JSFunctionProperty -> sourceElement.returnType
        is JSProperty -> {
          val functionInitializer = sourceElement.tryGetFunctionInitializer()
          if (functionInitializer != null) {
            provider = functionInitializer
            functionInitializer.returnType
          }
          else {
            sourceElement.jsType?.let {
              JSApplyCallType(it, JSTypeSourceFactory.createTypeSource(sourceElement, false))
            }
          }
        }
        else -> null
      }
      source = VueImplicitElement(name, returnType, provider, JSImplicitElement.Type.Property, true)
      type = source.jsType
    }

    override fun createPointer(): Pointer<VueSourceComputedProperty> =
      createPointer(::VueSourceComputedProperty)

  }

  private class VueSourceMethod(
    name: String,
    source: PsiElement,
  ) : VueSourceProperty(name, source), VueMethod {

    override val type: JSType?
      get() = JSResolveUtil.getElementJSType(source)

    override fun createPointer(): Pointer<VueSourceMethod> =
      createPointer(::VueSourceMethod)
  }

  private data class VueSourceEmitDefinition(
    override val name: String,
    override val source: PsiElement,
  ) : VueEmitCall, PsiSourcedPolySymbol {

    override val searchTarget: PolySymbolSearchTarget
      get() = PolySymbolSearchTarget.create(this)

    override fun createPointer(): Pointer<VueSourceEmitDefinition> {
      val name = name
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { VueSourceEmitDefinition(name, it) }
      }
    }

  }

  private data class VueSourceSlot(
    override val name: String,
    override val source: PsiElement,
  ) : VueSlot, PsiSourcedPolySymbol {
    override val type: JSType? = source.asSafely<JSTypeOwner>()?.jsType

    override fun createPointer(): Pointer<VueSourceSlot> {
      val name = name
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        val source = sourcePtr.dereference() ?: return@Pointer null
        VueSourceSlot(name, source)
      }
    }
  }

  private data class VueSourceInject(
    override val name: String,
    override val source: PsiElement,
  ) : VueInject, PsiSourcedPolySymbol {

    private val keyType: VueInjectKey? by lazy(LazyThreadSafetyMode.PUBLICATION) {
      getInjectionKeyType(source.asSafely<JSProperty>()?.initializerOrStub)
    }

    override val from: String?
      get() = keyType?.name

    override val injectionKey: PsiNamedElement?
      get() = keyType?.symbol

    override val defaultValue: JSType?
      get() = getInjectDefaultType(source.asSafely<JSProperty>()?.initializerOrStub)

    override fun createPointer(): Pointer<VueSourceInject> {
      val name = name
      val sourcePointer = source.createSmartPointer()
      return Pointer {
        VueSourceInject(name, sourcePointer.dereference() ?: return@Pointer null)
      }
    }

    private data class VueInjectKey(val name: String? = null, val symbol: PsiNamedElement? = null)

    private fun getInjectionKeyType(options: JSExpression?): VueInjectKey? {
      val property = (options as? JSObjectLiteralExpression)?.findProperty(INJECT_FROM) ?: return null
      val propertyType = property.jsType?.let { type -> if (type is JSWidenType) type.originalType else type }?.substitute()
      return when {
        propertyType is JSStringLiteralTypeImpl -> VueInjectKey(name = propertyType.literal)
        isInjectionSymbolType(propertyType) -> resolveInjectionSymbol(property.initializerOrStub)?.let { VueInjectKey(symbol = it) }
        else -> null
      }
    }

    private fun getInjectDefaultType(expression: JSExpression?): JSType? =
      when (val defaultType = getDefaultTypeFromPropOptions(expression)) {
        is JSFunctionType -> defaultType.returnType?.substitute()
        else -> defaultType
      }
  }
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSComputedPropertyNameOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.JSWidenType
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponents.Companion.getComponentDescriptor
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
    override val components: Map<String, VueComponent> get() = get(Holder.COMPONENTS)
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
    val PROPS = SimpleMemberAccessor(ContainerMember.Props, ::VueSourceInputProperty)
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
        ?.mapNotNull { VueComponents.vueMixinDescriptorFinder(it) }
        ?.mapNotNull { VueModelManager.getMixin(it) }
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
          .mapNotNull { VueComponents.vueMixinDescriptorFinder(it) }
          .mapNotNull { VueModelManager.getMixin(it) }
          .toList()

      val initializerMixins: List<VueMixin> =
        (mixinsProperty as? StubBasedPsiElement<*>)?.stub
          ?.getChildrenByType(JSStubElementTypes.OBJECT_LITERAL_EXPRESSION, JSObjectLiteralExpression.ARRAY_FACTORY)
          ?.mapNotNull { VueModelManager.getMixin(it) }
        ?: (mixinsProperty.value as? JSArrayLiteralExpression)
          ?.expressions
          ?.asSequence()
          ?.filterIsInstance<JSObjectLiteralExpression>()
          ?.mapNotNull { VueModelManager.getMixin(it) }
          ?.toList()
        ?: emptyList()
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

  private class ComponentsAccessor : MapAccessor<VueComponent>() {
    override fun build(declaration: JSElement): Map<String, VueComponent> {
      return StreamEx.of(Holder.ContainerMember.Components.readMembers(declaration))
        .mapToEntry({ p -> p.first }, { p -> p.second })
        .mapValues { element ->
          when (val meaningfulElement = VueComponents.meaningfulExpression(element) ?: element) {
            is ES6ImportedBinding ->
              meaningfulElement.declaration?.fromClause
                ?.resolveReferencedElements()
                ?.find { it is JSEmbeddedContent }
                ?.context
                ?.asSafely<XmlTag>()
                ?.takeIf { hasAttribute(it, SETUP_ATTRIBUTE_NAME) }
                ?.containingFile
                ?.let { VueModelManager.getComponent(it) }
            is HtmlFileImpl ->
              VueModelManager.getComponent(meaningfulElement)
            else -> getComponentDescriptor(meaningfulElement as? JSElement)
              ?.let { VueModelManager.getComponent(it) }
          }?.let {
            if (element is JSPsiNamedElementBase && it is VueRegularComponent) {
              VueLocallyDefinedRegularComponent(it, element)
            }
            else it
          }
          ?: VueUnresolvedComponent(declaration, element, element.name)
        }
        .distinctKeys()
        .into(mutableMapOf<String, VueComponent>())
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


  class VueSourceInputProperty(
    override val name: String,
    sourceElement: PsiElement,
    hasOuterDefault: Boolean = false,
  ) : VueInputProperty {

    override val required: Boolean = isRequired(hasOuterDefault, sourceElement)

    override val source: VueImplicitElement =
      VueImplicitElement(name, createType(sourceElement, isOptional(sourceElement)),
                         sourceElement, JSImplicitElement.Type.Property, true)

    override val jsType: JSType? = createType(sourceElement, !required)

    private fun createType(sourceElement: PsiElement, optional: Boolean) =
      (sourceElement as? PsiNamedElement)?.let { VueSourcePropType(it) }?.optionalIf(optional)

    override fun toString(): String {
      return "VueSourceInputProperty(name='$name', required=$required, jsType=$jsType)"
    }

    private fun isRequired(hasOuterDefault: Boolean, sourceElement: PsiElement?): Boolean {
      // script setup defineProps runtime declarations rely on this class (see VueScriptSetupInfoProvider)
      // withDefaults call is incompatible, but defaults from props destructure should work
      if (hasOuterDefault) return false
      return getRequiredFromPropOptions((sourceElement as? JSProperty)?.initializerOrStub)
    }

    private fun isOptional(sourceElement: PsiElement?): Boolean =
      getPropOptionality((sourceElement as? JSProperty)?.initializerOrStub, required)
  }

  private class VueSourceDataProperty(
    override val name: String,
    override val source: PsiElement?,
  ) : VueDataProperty

  private class VueSourceComputedProperty(
    override val name: String,
    sourceElement: PsiElement,
  ) : VueComputedProperty {
    override val source: VueImplicitElement
    override val jsType: JSType?

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
      jsType = source.jsType
    }

  }

  private class VueSourceMethod(
    override val name: String,
    override val source: PsiElement?,
  ) : VueMethod {
    override val jsType: JSType? get() = (source as? JSProperty)?.jsType
  }

  private class VueSourceEmitDefinition(
    override val name: String,
    override val source: PsiElement?,
  ) : VueEmitCall

  private class VueSourceSlot(override val name: String, override val source: PsiElement?) : VueSlot {
    override val scope: JSType? = source.asSafely<JSTypeOwner>()?.jsType
  }

  private class VueSourceInject(override val name: String, override val source: PsiElement?) : VueInject {

    private val keyType: VueInjectKey? by lazy(LazyThreadSafetyMode.PUBLICATION) {
      getInjectionKeyType(source.asSafely<JSProperty>()?.initializerOrStub)
    }

    override val from: String?
      get() = keyType?.name

    override val injectionKey: PsiNamedElement?
      get() = keyType?.symbol

    override val defaultValue: JSType?
      get() = getInjectDefaultType(source.asSafely<JSProperty>()?.initializerOrStub)

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

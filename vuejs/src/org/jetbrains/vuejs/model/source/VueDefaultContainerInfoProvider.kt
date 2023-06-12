// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
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
import org.jetbrains.vuejs.types.VueUnwrapRefType
import org.jetbrains.vuejs.types.optionalIf

class VueDefaultContainerInfoProvider : VueContainerInfoProvider.VueInitializedContainerInfoProvider(::VueSourceContainerInfo) {

  private class VueSourceContainerInfo(declaration: JSElement) : VueInitializedContainerInfo(declaration) {
    override val data: List<VueDataProperty> get() = get(DATA)
    override val computed: List<VueComputedProperty> get() = get(COMPUTED)
    override val methods: List<VueMethod> get() = get(METHODS)
    override val props: List<VueInputProperty> get() = get(PROPS)
    override val emits: List<VueEmitCall> get() = get(EMITS)

    override val model: VueModelDirectiveProperties get() = get(MODEL)

    override val delimiters: Pair<String, String>? get() = get(DELIMITERS).get()
    override val extends: List<VueMixin> get() = get(EXTENDS) + get(EXTENDS_CALL)
    override val components: Map<String, VueComponent> get() = get(COMPONENTS)
    override val directives: Map<String, VueDirective> get() = get(DIRECTIVES)
    override val mixins: List<VueMixin> get() = get(MIXINS)
    override val filters: Map<String, VueFilter> get() = get(FILTERS)
    override val provide: List<VueProvide> get() = get(PROVIDE)
    override val inject: List<VueInject> get() = get(INJECT)

  }

  companion object {

    private val ContainerMember = object {
      val Props: MemberReader = MemberReader(PROPS_PROP, true)
      val Computed = MemberReader(COMPUTED_PROP)
      val Methods = MemberReader(METHODS_PROP)
      val Emits = MemberReader(EMITS_PROP, true, false)
      val Directives = MemberReader(DIRECTIVES_PROP)
      val Components = MemberReader(COMPONENTS_PROP)
      val Filters = MemberReader(FILTERS_PROP)
      val Delimiters = MemberReader(DELIMITERS_PROP, true, false)
      val Model = MemberReader(MODEL_PROP)
      val Data = MemberReader(DATA_PROP, canBeFunctionResult = true)
      val Provide = MemberReader(PROVIDE_PROP, canBeFunctionResult = true)
      val Inject = MemberReader(INJECT_PROP, canBeArray = true)
    }

    private val EXTENDS = MixinsAccessor(EXTENDS_PROP, VueExtendsBindingIndex.KEY)
    private val EXTENDS_CALL = ExtendsCallAccessor()
    private val MIXINS = MixinsAccessor(MIXINS_PROP, VueMixinBindingIndex.KEY)
    private val DIRECTIVES = DirectivesAccessor()
    private val COMPONENTS = ComponentsAccessor()
    private val FILTERS = SimpleMemberMapAccessor(ContainerMember.Filters, ::VueSourceFilter)
    private val DELIMITERS = DelimitersAccessor()
    private val PROVIDE = SimpleMemberAccessor(ContainerMember.Provide, ::VueSourceProvide)
    private val INJECT = SimpleMemberAccessor(ContainerMember.Inject, ::VueSourceInject)

    private val PROPS = SimpleMemberAccessor(ContainerMember.Props, ::VueSourceInputProperty)
    private val DATA = SimpleMemberAccessor(ContainerMember.Data, ::VueSourceDataProperty)
    private val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VueSourceComputedProperty)
    private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VueSourceMethod)
    private val EMITS = SimpleMemberAccessor(ContainerMember.Emits, ::VueSourceEmitDefinition)

    private val MODEL = ModelAccessor()
  }

  private class ExtendsCallAccessor : ListAccessor<VueMixin>() {
    override fun build(declaration: JSElement): List<VueMixin> =
      declaration.context
        ?.let { if (it is JSArgumentList) it.context else it }
        ?.asSafely<JSCallExpression>()
        ?.indexingData
        ?.implicitElements
        ?.asSequence()
        ?.filter { it.userString == VueExtendsBindingIndex.JS_KEY }
        ?.mapNotNull { VueComponents.vueMixinDescriptorFinder(it) }
        ?.mapNotNull { VueModelManager.getMixin(it) }
        ?.toList()
      ?: emptyList()
  }

  private class MixinsAccessor(private val propertyName: String,
                               private val indexKey: StubIndexKey<String, JSImplicitElementProvider>)
    : ListAccessor<VueMixin>() {

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
          ?.getChildrenByType(JSElementTypes.OBJECT_LITERAL_EXPRESSION, JSObjectLiteralExpression.ARRAY_FACTORY)
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
      return StreamEx.of(ContainerMember.Directives.readMembers(declaration))
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
      return StreamEx.of(ContainerMember.Components.readMembers(declaration))
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

  private class ModelAccessor : MemberAccessor<VueModelDirectiveProperties>() {
    override fun build(declaration: JSElement): VueModelDirectiveProperties {
      var prop: String? = null
      var event: String? = null
      ContainerMember.Model.readMembers(declaration).forEach { (name, element) ->
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
      val delimiters = ContainerMember.Delimiters.readMembers(declaration)
      if (delimiters.size == 2
          && delimiters[0].first.isNotBlank()
          && delimiters[1].first.isNotBlank()) {
        return Ref(Pair(delimiters[0].first, delimiters[1].first))
      }
      return Ref(null)
    }
  }


  class VueSourceInputProperty(override val name: String,
                               sourceElement: PsiElement,
                               hasOuterDefault: Boolean = false) : VueInputProperty {

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

  private class VueSourceDataProperty(override val name: String,
                                      override val source: PsiElement?) : VueDataProperty

  private class VueSourceComputedProperty(override val name: String,
                                          sourceElement: PsiElement) : VueComputedProperty {
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

  private class VueSourceMethod(override val name: String,
                                override val source: PsiElement?) : VueMethod {
    override val jsType: JSType? get() = (source as? JSProperty)?.jsType
  }

  private class VueSourceEmitDefinition(override val name: String,
                                        override val source: PsiElement?) : VueEmitCall

  private class VueSourceInject(override val name: String, override val source: PsiElement?) : VueInject {
    override val from: String? = getInjectAliasName(source.asSafely<JSProperty>()?.initializerOrStub)
  }

  private class VueSourceProvide(override val name: String, override val source: PsiElement?) : VueProvide {
    override val jsType: JSType? = if (source is JSTypeOwner) {
      source.jsType?.let { VueUnwrapRefType(it, source) }
    }
    else {
      null
    }
  }
}

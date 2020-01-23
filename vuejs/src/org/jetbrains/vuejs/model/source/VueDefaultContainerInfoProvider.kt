// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions
import org.jetbrains.vuejs.codeInsight.getRequiredFromPropOptions
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*

class VueDefaultContainerInfoProvider : VueContainerInfoProvider.VueInitializedContainerInfoProvider(::VueSourceContainerInfo) {

  private class VueSourceContainerInfo(declaration: JSObjectLiteralExpression) : VueInitializedContainerInfo(declaration) {
    override val data: List<VueDataProperty> get() = get(DATA)
    override val computed: List<VueComputedProperty> get() = get(COMPUTED)
    override val methods: List<VueMethod> get() = get(METHODS)
    override val props: List<VueInputProperty> get() = get(PROPS)

    override val model: VueModelDirectiveProperties get() = get(MODEL)

    override val delimiters: Pair<String, String>? get() = get(DELIMITERS)
    override val extends: List<VueMixin> get() = get(EXTENDS)
    override val components: Map<String, VueComponent> get() = get(COMPONENTS)
    override val directives: Map<String, VueDirective> get() = get(DIRECTIVES)
    override val mixins: List<VueMixin> get() = get(MIXINS)
    override val filters: Map<String, VueFilter> get() = get(FILTERS)

  }

  companion object {

    private val ContainerMember = object {
      val Props: MemberReader = MemberReader("props", true)
      val Computed = MemberReader("computed")
      val Methods = MemberReader("methods")
      val Directives = MemberReader("directives")
      val Components = MemberReader("components")
      val Filters = MemberReader("filters")
      val Delimiters = MemberReader("delimiters", true, false)
      val Model = MemberReader("model")
      val Data = object : MemberReader("data") {
        override fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? =
          findReturnedObjectLiteral(resolved)

        override fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? {
          val function = property.tryGetFunctionInitializer() ?: return null
          return findReturnedObjectLiteral(function)
        }
      }

      private fun findReturnedObjectLiteral(resolved: PsiElement): JSObjectLiteralExpression? {
        if (resolved !is JSFunction) return null
        return JSStubBasedPsiTreeUtil.findDescendants<JSObjectLiteralExpression>(
          resolved, TokenSet.create(
          JSStubElementTypes.OBJECT_LITERAL_EXPRESSION))
          .find {
            it.context == resolved ||
            it.context is JSParenthesizedExpression && it.context?.context == resolved ||
            it.context is JSReturnStatement
          }
      }

    }

    private val EXTENDS = MixinsAccessor(EXTENDS_PROP, VueExtendsBindingIndex.KEY)
    private val MIXINS = MixinsAccessor(MIXINS_PROP, VueMixinBindingIndex.KEY)
    private val DIRECTIVES = DirectivesAccessor()
    private val COMPONENTS = ComponentsAccessor()
    private val FILTERS = SimpleMemberMapAccessor(ContainerMember.Filters, ::VueSourceFilter)
    private val DELIMITERS = DelimitersAccessor()

    private val PROPS = SimpleMemberAccessor(ContainerMember.Props, ::VueSourceInputProperty)
    private val DATA = SimpleMemberAccessor(ContainerMember.Data, ::VueSourceDataProperty)
    private val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VueSourceComputedProperty)
    private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VueSourceMethod)

    private val MODEL = ModelAccessor()
  }

  private class MixinsAccessor(private val propertyName: String,
                               private val indexKey: StubIndexKey<String, JSImplicitElementProvider>)
    : ListAccessor<VueMixin>() {

    override fun build(declaration: JSObjectLiteralExpression): List<VueMixin> {
      val mixinsProperty = declaration.findProperty(propertyName) ?: return emptyList()
      val elements = resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile.originalFile), indexKey)
                     ?: return emptyList()
      val original = CompletionUtil.getOriginalOrSelf<PsiElement>(mixinsProperty)
      return StreamEx.of(elements)
        .filter { PsiTreeUtil.isAncestor(original, it.parent, false) }
        .map { VueComponents.vueMixinDescriptorFinder(it) }
        .nonNull()
        .map { VueModelManager.getMixin(it!!) }
        .nonNull()
        .toList()
    }
  }

  private class DirectivesAccessor : MapAccessor<VueDirective>() {
    override fun build(declaration: JSObjectLiteralExpression): Map<String, VueDirective> {
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
    override fun build(declaration: JSObjectLiteralExpression): Map<String, VueComponent> {
      return StreamEx.of(ContainerMember.Components.readMembers(declaration))
        .mapToEntry({ p -> p.first }, { p -> p.second })
        .mapValues { element ->
          (VueComponents.meaningfulExpression(element) ?: element)
            .let { meaningfulElement ->
              objectLiteralFor(meaningfulElement)
              ?: (meaningfulElement.parent as? ES6ExportDefaultAssignment)
                ?.let { VueComponents.getExportedDescriptor(it) }
                ?.let { it.obj ?: it.clazz }
            }
            ?.let { VueModelManager.getComponent(it) }
          ?: VueUnresolvedComponent()
        }
        .distinctKeys()
        .into(mutableMapOf<String, VueComponent>())
    }
  }

  private class ModelAccessor : MemberAccessor<VueModelDirectiveProperties>() {
    override fun build(declaration: JSObjectLiteralExpression): VueModelDirectiveProperties {
      var prop = VueModelDirectiveProperties.DEFAULT_PROP
      var event = VueModelDirectiveProperties.DEFAULT_EVENT
      ContainerMember.Model.readMembers(declaration).forEach { (name, element) ->
        (element as? JSProperty)?.value
          ?.let { getTextIfLiteral(it) }
          ?.let { value ->
            if (name == "prop")
              prop = value
            else if (name == "event")
              event = value
          }
      }
      return VueModelDirectiveProperties(prop, event)
    }
  }

  private class DelimitersAccessor : MemberAccessor<Pair<String, String>?>() {
    override fun build(declaration: JSObjectLiteralExpression): Pair<String, String>? {
      val delimiters = ContainerMember.Delimiters.readMembers(declaration)
      if (delimiters.size == 2
          && delimiters[0].first.isNotBlank()
          && delimiters[1].first.isNotBlank()) {
        return Pair(delimiters[0].first, delimiters[1].first)
      }
      return null
    }
  }


  private class VueSourceInputProperty(override val name: String,
                                       sourceElement: PsiElement?) : VueInputProperty {

    override val source: JSLocalImplicitElementImpl =
      JSLocalImplicitElementImpl(name, getJSTypeFromPropOptions((sourceElement as? JSProperty)?.value),
                                 sourceElement, JSImplicitElement.Type.Property)
    override val jsType: JSType? = source.jsType
    override val required: Boolean = getRequiredFromPropOptions((sourceElement as? JSProperty)?.value)
  }

  private class VueSourceDataProperty(override val name: String,
                                      override val source: PsiElement?) : VueDataProperty

  private class VueSourceComputedProperty(override val name: String,
                                          sourceElement: PsiElement?) : VueComputedProperty {
    override val source: JSLocalImplicitElementImpl
    override val jsType: JSType?

    init {
      val functionSource = (sourceElement as? JSProperty)?.tryGetFunctionInitializer() ?: sourceElement
      source = JSLocalImplicitElementImpl(name, (functionSource as? JSFunctionItem)?.returnType,
                                          functionSource, JSImplicitElement.Type.Property)
      jsType = source.jsType
    }

  }

  private class VueSourceMethod(override val name: String,
                                override val source: PsiElement?) : VueMethod

}

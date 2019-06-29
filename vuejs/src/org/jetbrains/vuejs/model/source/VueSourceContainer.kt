// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.*
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.attributes.findProperty
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*

abstract class VueSourceContainer(sourceElement: PsiElement,
                                  protected val declaration: JSObjectLiteralExpression?) : VueContainer {

  override val source: PsiElement? = sourceElement
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val template: PsiElement? = null
  override val element: String? get() = getTextIfLiteral(declaration?.findProperty("el")?.value)

  override val data: List<VueDataProperty> get() = DATA.get(declaration)
  override val computed: List<VueComputedProperty> get() = COMPUTED.get(declaration)
  override val methods: List<VueMethod> get() = METHODS.get(declaration)
  override val props: List<VueInputProperty> get() = PROPS.get(declaration)

  override val emits: List<VueEmitCall> = emptyList()
  override val slots: List<VueSlot> = emptyList()

  override val extends: List<VueContainer> get() = EXTENDS.get(declaration)
  override val components: Map<String, VueComponent> get () = COMPONENTS.get(declaration)
  override val directives: Map<String, VueDirective> get() = DIRECTIVES.get(declaration)
  override val mixins: List<VueMixin> get() = MIXINS.get(declaration)
  override val filters: Map<String, VueFilter> = emptyMap()

  companion object {
    private val EXTENDS = MixinsAccessor(EXTENDS_PROP, VueExtendsBindingIndex.KEY)
    private val MIXINS = MixinsAccessor(MIXINS_PROP, VueMixinBindingIndex.KEY)
    private val DIRECTIVES = DirectivesAccessor()
    private val COMPONENTS = ComponentsAccessor()

    private val PROPS = SimpleMemberAccessor(ContainerMember.Props, ::VueSourceInputProperty)
    private val DATA = SimpleMemberAccessor(ContainerMember.Data, ::VueSourceDataProperty)
    private val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VueSourceComputedProperty)
    private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VueSourceMethod)
  }

  private abstract class MemberAccessor<T> {

    open val key: Key<CachedValue<T>> = Key("vuejs.member." + javaClass.name)

    fun get(declaration: JSObjectLiteralExpression?): T {
      return if (declaration != null)
        CachedValuesManager.getCachedValue(declaration, key) {
          CachedValueProvider.Result.create(build(declaration), PsiModificationTracker.MODIFICATION_COUNT)
        }
      else empty()
    }

    protected abstract fun build(declaration: JSObjectLiteralExpression): T

    protected abstract fun empty(): T

  }

  private abstract class ListAccessor<T> : MemberAccessor<List<T>>() {
    override fun empty(): List<T> {
      return emptyList()
    }
  }

  private abstract class MapAccessor<T> : MemberAccessor<Map<String, T>>() {
    override fun empty(): Map<String, T> {
      return emptyMap()
    }
  }

  private class MixinsAccessor(private val propertyName: String,
                               private val indexKey: StubIndexKey<String, JSImplicitElementProvider>) : ListAccessor<VueMixin>() {

    override val key: Key<CachedValue<List<VueMixin>>> = Key("vuejs.member.$propertyName")

    override fun build(declaration: JSObjectLiteralExpression): List<VueMixin> {
      val mixinsProperty = findProperty(declaration, propertyName) ?: return emptyList()
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
      val directives = findProperty(declaration, DIRECTIVES_PROP)
      val fileScope = VueDirectivesProvider.createContainingFileScope(directives)
      return if (directives != null && fileScope != null) {
        StreamEx.of(getForAllKeys(fileScope, VueLocalDirectivesIndex.KEY))
          .filter { PsiTreeUtil.isAncestor(directives, it.parent, false) }
          .mapToEntry({ it.name }, { VueSourceDirective(it.name, it.parent) as VueDirective })
          // TODO properly support multiple directives with the same name
          .distinctKeys()
          .into(mutableMapOf<String, VueDirective>())
      }
      else {
        emptyMap()
      }
    }
  }

  private class ComponentsAccessor : MapAccessor<VueComponent>() {
    override fun build(declaration: JSObjectLiteralExpression): Map<String, VueComponent> {
      return StreamEx.of(ContainerMember.Components.readMembers(declaration))
        .mapToEntry({ p -> p.first }, { p -> p.second })
        .mapValues { element ->
          (VueComponents.meaningfulExpression(element) ?: element)
            .let { meaningfulElement ->
              VueComponentsCalculation.getObjectLiteralFromResolve(listOf(meaningfulElement))
              ?: (meaningfulElement.parent as? ES6ExportDefaultAssignment)
                ?.let { VueComponents.getExportedDescriptor(it) }
                ?.let { it.obj ?: it.clazz }
            }
            ?.let { VueModelManager.getComponent(it) }
          ?: VueUnresolvedComponent()
        }
        // TODO properly support multiple components with the same name
        .distinctKeys()
        .into(mutableMapOf<String, VueComponent>())
    }
  }

  private class SimpleMemberAccessor<T>(val member: ContainerMember, val provider: (String, JSElement) -> T) : ListAccessor<T>() {
    override val key: Key<CachedValue<List<T>>> = Key("vuejs.member." + member.name)

    override fun build(declaration: JSObjectLiteralExpression): List<T> {
      return member.readMembers(declaration).map { (name, element) -> provider(name, element) }
    }
  }

  private enum class ContainerMember(val propertyName: String,
                                     val isFunctions: Boolean,
                                     private val canBeArray: Boolean) {
    Props("props", false, true),
    Computed("computed", true, false),
    Methods("methods", true, false),
    Components("components", false, false),
    Data("data", false, false) {
      override fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? = findReturnedObjectLiteral(resolved)

      override fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? {
        val function = property.tryGetFunctionInitializer() ?: return null
        return findReturnedObjectLiteral(function)
      }
    };

    fun readMembers(descriptor: JSObjectLiteralExpression): List<Pair<String, JSElement>> {
      val detailsFilter = if (isFunctions) FUNCTION_FILTER else { _: PsiElement -> true }
      val property = descriptor.findProperty(propertyName) ?: return emptyList()

      var propsObject = property.objectLiteralExpressionInitializer ?: getObjectLiteral(property)
      if (propsObject == null && property.initializerReference != null) {
        val resolved = JSStubBasedPsiTreeUtil.resolveLocally(property.initializerReference!!, property)
        if (resolved != null) {
          propsObject = JSStubBasedPsiTreeUtil.findDescendants(resolved, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
                          .find { it.context == resolved } ?: getObjectLiteralFromResolved(resolved)
          if (propsObject == null && canBeArray) {
            return readPropsFromArray(resolved, detailsFilter)
          }
        }
      }
      if (propsObject != null) {
        return filteredObjectProperties(propsObject, detailsFilter)
      }
      return if (canBeArray) readPropsFromArray(property, detailsFilter) else return emptyList()
    }

    protected open fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? = null
    protected open fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? = null


    private fun filteredObjectProperties(propsObject: JSObjectLiteralExpression, filter: (PsiElement) -> Boolean) =
      propsObject.properties.filter {
        val propName = it.name
        propName != null && filter(it)
      }.map { Pair(it.name!!, it) }

    private fun readPropsFromArray(holder: PsiElement, filter: (PsiElement) -> Boolean): List<Pair<String, JSElement>> =
      getStringLiteralsFromInitializerArray(holder) { _, element -> filter(element) }
        .map { Pair(it.stringValue ?: "", it) }

    companion object {
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

      private val FUNCTION_FILTER = { element: PsiElement ->
        element is JSFunctionProperty || element is JSProperty && element.value is JSFunction
      }
    }
  }
}

class VueSourceInputProperty(override val name: String,
                             override val source: PsiElement?) : VueInputProperty


class VueSourceDataProperty(override val name: String,
                            override val source: PsiElement?) : VueDataProperty

class VueSourceComputedProperty(override val name: String,
                                override val source: PsiElement?) : VueComputedProperty

class VueSourceMethod(override val name: String,
                      override val source: PsiElement?) : VueMethod

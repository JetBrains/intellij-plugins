// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.*
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*

abstract class VueSourceContainer(sourceElement: JSImplicitElement,
                                  private val clazz: JSClass?,
                                  protected val initializer: JSObjectLiteralExpression?) : VueContainer {

  override val source: PsiElement = sourceElement
  override val parents: List<VueEntitiesContainer> get() = VueGlobalImpl.getParents(this)

  override val template: PsiElement? = null
  override val element: String? get() = getTextIfLiteral(initializer?.findProperty("el")?.value)

  override val data: List<VueDataProperty> get() = get(DATA)
  override val computed: List<VueComputedProperty> get() = get(COMPUTED)
  override val methods: List<VueMethod> get() = get(METHODS)
  override val props: List<VueInputProperty> get() = get(PROPS)

  override val model: VueModelDirectiveProperties get() = get(MODEL)

  override val emits: List<VueEmitCall> get() = VueDecoratedComponentInfo.get(clazz)?.emits ?: emptyList()
  override val slots: List<VueSlot> = emptyList()

  override val delimiters: Pair<String, String>? get() = get(DELIMITERS)
  override val extends: List<VueContainer> get() = get(EXTENDS)
  override val components: Map<String, VueComponent> get() = get(COMPONENTS)
  override val directives: Map<String, VueDirective> get() = get(DIRECTIVES)
  override val mixins: List<VueMixin> get() = get(MIXINS)
  override val filters: Map<String, VueFilter> = emptyMap()

  private fun <T> get(accessor: MemberAccessor<T>): T {
    return accessor.get(initializer, VueDecoratedComponentInfo.get(clazz))
  }

  companion object {
    private val EXTENDS = MixinsAccessor(EXTENDS_PROP, VueExtendsBindingIndex.KEY, VueDecoratedComponentInfo::extends)
    private val MIXINS = MixinsAccessor(MIXINS_PROP, VueMixinBindingIndex.KEY, VueDecoratedComponentInfo::mixins)
    private val DIRECTIVES = DirectivesAccessor()
    private val COMPONENTS = ComponentsAccessor()
    private val DELIMITERS = DelimitersAccessor()

    private val PROPS = SimpleMemberAccessor(ContainerMember.Props, ::VueSourceInputProperty, VueDecoratedComponentInfo::props)
    private val DATA = SimpleMemberAccessor(ContainerMember.Data, ::VueSourceDataProperty, VueDecoratedComponentInfo::data)
    private val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VueSourceComputedProperty, VueDecoratedComponentInfo::computed)
    private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VueSourceMethod, VueDecoratedComponentInfo::methods)

    private val MODEL = ModelAccessor(VueDecoratedComponentInfo::model)
  }

  private abstract class MemberAccessor<T>(val decoratedAccessor: ((VueDecoratedComponentInfo) -> T?)?) {

    open val key: Key<CachedValue<T>> = Key("vuejs.member." + javaClass.name)

    fun get(declaration: JSObjectLiteralExpression?, decoratedComponentInfo: VueDecoratedComponentInfo?): T {
      return if (declaration != null) {
        val fromInitializer = CachedValuesManager.getCachedValue(declaration, key) {
          CachedValueProvider.Result.create(build(declaration), PsiModificationTracker.MODIFICATION_COUNT)
        }
        if (decoratedComponentInfo != null && decoratedAccessor != null) {
          merge(decoratedAccessor.invoke(decoratedComponentInfo), fromInitializer)
        }
        else {
          fromInitializer
        }
      }
      else if (decoratedComponentInfo != null && decoratedAccessor != null) {
        return decoratedAccessor.invoke(decoratedComponentInfo) ?: empty()
      }
      else
        empty()
    }

    protected abstract fun build(declaration: JSObjectLiteralExpression): T

    protected abstract fun empty(): T

    protected abstract fun merge(fromClass: T?, fromInitializer: T): T

  }

  private abstract class ListAccessor<T>(decoratedAccessor: ((VueDecoratedComponentInfo) -> List<T>)?)
    : MemberAccessor<List<T>>(decoratedAccessor) {
    override fun empty(): List<T> {
      return emptyList()
    }

    override fun merge(fromClass: List<T>?, fromInitializer: List<T>): List<T> {
      if (fromClass == null || fromClass.isEmpty()) {
        return fromInitializer
      }
      if (fromInitializer.isEmpty()) {
        return fromClass
      }
      return StreamEx.of(fromClass).append(fromInitializer).distinct(::keyExtractor).toList()
    }

    abstract fun keyExtractor(obj: T): Any

  }

  private abstract class MapAccessor<T>(decoratedAccessor: ((VueDecoratedComponentInfo) -> Map<String, T>)?)
    : MemberAccessor<Map<String, T>>(decoratedAccessor) {
    override fun empty(): Map<String, T> {
      return emptyMap()
    }

    override fun merge(fromClass: Map<String, T>?, fromInitializer: Map<String, T>): Map<String, T> {
      if (fromClass == null || fromClass.isEmpty()) {
        return fromInitializer
      }
      if (fromInitializer.isEmpty()) {
        return fromClass
      }
      val result = fromClass.toMutableMap()
      fromInitializer.forEach { (key, value) -> result.putIfAbsent(key, value) }
      return result
    }
  }

  private class MixinsAccessor(private val propertyName: String,
                               private val indexKey: StubIndexKey<String, JSImplicitElementProvider>,
                               decoratedAccessor: ((VueDecoratedComponentInfo) -> List<VueMixin>))
    : ListAccessor<VueMixin>(decoratedAccessor) {

    override val key: Key<CachedValue<List<VueMixin>>> = Key("vuejs.member.$propertyName")

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

    override fun keyExtractor(obj: VueMixin): Any {
      return obj
    }
  }

  private class DirectivesAccessor : MapAccessor<VueDirective>(null) {
    override fun build(declaration: JSObjectLiteralExpression): Map<String, VueDirective> {
      val directives = declaration.findProperty(DIRECTIVES_PROP)
      val fileScope = createContainingFileScope(directives)
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

  private class ComponentsAccessor : MapAccessor<VueComponent>(null) {
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

  private class SimpleMemberAccessor<T : VueNamedSymbol>(val member: ContainerMember,
                                                         val provider: (String, JSElement) -> T,
                                                         decoratedAccessor: ((VueDecoratedComponentInfo) -> List<T>))
    : ListAccessor<T>(decoratedAccessor) {

    override val key: Key<CachedValue<List<T>>> = Key("vuejs.member." + member.name)

    override fun build(declaration: JSObjectLiteralExpression): List<T> {
      return member.readMembers(declaration).map { (name, element) -> provider(name, element) }
    }

    override fun keyExtractor(obj: T): Any {
      return obj.name
    }
  }

  private class ModelAccessor(decoratedAccessor: ((VueDecoratedComponentInfo) -> VueModelDirectiveProperties?))
    : MemberAccessor<VueModelDirectiveProperties>(decoratedAccessor) {

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

    override fun empty(): VueModelDirectiveProperties {
      return VueModelDirectiveProperties()
    }

    override fun merge(fromClass: VueModelDirectiveProperties?, fromInitializer: VueModelDirectiveProperties): VueModelDirectiveProperties {
      return fromClass ?: fromInitializer
    }
  }

  private class DelimitersAccessor : MemberAccessor<Pair<String, String>?>({ null }) {
    override fun build(declaration: JSObjectLiteralExpression): Pair<String, String>? {
      val delimiters = ContainerMember.Delimiters.readMembers(declaration)
      if (delimiters.size == 2
          && delimiters[0].first.isNotBlank()
          && delimiters[1].first.isNotBlank()) {
        return Pair(delimiters[0].first, delimiters[1].first)
      }
      return null
    }

    override fun empty(): Pair<String, String>? {
      return null
    }

    override fun merge(fromClass: Pair<String, String>?, fromInitializer: Pair<String, String>?): Pair<String, String>? {
      return fromInitializer
    }
  }

  private enum class ContainerMember(val propertyName: String,
                                     val isFunctions: Boolean,
                                     private val canBeArray: Boolean) {
    Props("props", false, true),
    Computed("computed", true, false),
    Methods("methods", true, false),
    Components("components", false, false),
    Delimiters("delimiters", false, true),
    Model("model", false, false),
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
          if ((propsObject == null && canBeArray) || this === Delimiters) {
            return readPropsFromArray(resolved, detailsFilter)
          }
        }
      }
      if (propsObject != null && this !== Delimiters) {
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
                             override val source: PsiElement?) : VueInputProperty {
  override val jsType: JSType? = getJSTypeFromPropOptions((source as? JSProperty)?.value)
  override val required: Boolean = getRequiredFromPropOptions((source as? JSProperty)?.value)
}

class VueSourceDataProperty(override val name: String,
                            override val source: PsiElement?) : VueDataProperty

class VueSourceComputedProperty(override val name: String,
                                override val source: PsiElement?) : VueComputedProperty

class VueSourceMethod(override val name: String,
                      override val source: PsiElement?) : VueMethod

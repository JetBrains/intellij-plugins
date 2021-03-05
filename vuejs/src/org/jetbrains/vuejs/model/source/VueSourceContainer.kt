// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

abstract class VueSourceContainer(sourceElement: JSImplicitElement,
                                  override val descriptor: VueSourceEntityDescriptor)
  : UserDataHolderBase(), VueContainer, VueSourceEntity {

  override val source: PsiElement = sourceElement
  override val parents: List<VueEntitiesContainer> get() = VueGlobalImpl.getParents(this)

  override val element: String?
    get() = getTextIfLiteral(
      descriptor.initializer?.castSafelyTo<JSObjectLiteralExpression>()
        ?.findProperty(EL_PROP)?.literalExpressionInitializer)

  override val data: List<VueDataProperty> get() = get(DATA)
  override val computed: List<VueComputedProperty> get() = get(COMPUTED)
  override val methods: List<VueMethod> get() = get(METHODS)
  override val props: List<VueInputProperty> get() = get(PROPS)

  override val model: VueModelDirectiveProperties get() = get(MODEL)

  override val emits: List<VueEmitCall> get() = get(EMITS)
  override val template: VueTemplate<*>? get() = if (this is VueRegularComponent) get(TEMPLATE) else null
  override val slots: List<VueSlot> = emptyList()

  override val delimiters: Pair<String, String>? get() = get(DELIMITERS)
  override val extends: List<VueContainer> get() = get(EXTENDS)
  override val components: Map<String, VueComponent> get() = get(COMPONENTS)
  override val directives: Map<String, VueDirective> get() = get(DIRECTIVES)
  override val mixins: List<VueMixin> get() = get(MIXINS)
  override val filters: Map<String, VueFilter> get() = get(FILTERS)

  private fun <T> get(accessor: MemberAccessor<T>): T {
    return accessor.get(descriptor)
  }

  companion object {
    private val EXTENDS = ListAccessor(VueContainerInfo::extends)
    private val MIXINS = ListAccessor(VueContainerInfo::mixins)
    private val DIRECTIVES = MapAccessor(VueContainerInfo::directives)
    private val COMPONENTS = MapAccessor(VueContainerInfo::components)
    private val FILTERS = MapAccessor(VueContainerInfo::filters)
    private val DELIMITERS = DelimitersAccessor(VueContainerInfo::delimiters)

    private val PROPS = NamedListAccessor(VueContainerInfo::props)
    private val DATA = NamedListAccessor(VueContainerInfo::data)
    private val COMPUTED = NamedListAccessor(VueContainerInfo::computed)
    private val METHODS = NamedListAccessor(VueContainerInfo::methods)

    private val EMITS = NamedListAccessor(VueContainerInfo::emits)

    private val MODEL = ModelAccessor(VueContainerInfo::model)
    private val TEMPLATE = TemplateAccessor(VueContainerInfo::template)

    fun getTemplate(descriptor: VueSourceEntityDescriptor): VueTemplate<*>? {
      return TEMPLATE.get(descriptor)
    }
  }

  private abstract class MemberAccessor<T>(val extInfoAccessor: (VueContainerInfo) -> T?, val takeFirst: Boolean = false) {

    fun get(descriptor: VueSourceEntityDescriptor): T {
      descriptor.ensureValid()
      return VueContainerInfoProvider.getProviders()
               .asSequence()
               .mapNotNull { it.getInfo(descriptor)?.let(extInfoAccessor) }
               .let {
                 if (takeFirst) it.firstOrNull()
                 else it.reduce(::merge)
               }
             ?: empty()
    }

    protected abstract fun empty(): T

    protected open fun merge(arg1: T, arg2: T): T {
      throw UnsupportedOperationException()
    }

  }

  private open class ListAccessor<T>(extInfoAccessor: (VueContainerInfo) -> List<T>)
    : MemberAccessor<List<T>>(extInfoAccessor) {
    override fun empty(): List<T> {
      return emptyList()
    }

    override fun merge(arg1: List<T>, arg2: List<T>): List<T> {
      if (arg1.isEmpty()) return arg2
      if (arg2.isEmpty()) return arg1
      return arg1.asSequence().plus(arg2).distinctBy(::keyExtractor).toList()
    }

    open fun keyExtractor(obj: T): Any {
      return obj!!
    }

  }

  private class MapAccessor<T>(extInfoAccessor: (VueContainerInfo) -> Map<String, T>)
    : MemberAccessor<Map<String, T>>(extInfoAccessor) {

    override fun empty(): Map<String, T> {
      return emptyMap()
    }

    override fun merge(arg1: Map<String, T>, arg2: Map<String, T>): Map<String, T> {
      if (arg1.isEmpty()) return arg2
      if (arg2.isEmpty()) return arg1
      val result = arg1.toMutableMap()
      arg2.forEach { (key, value) -> result.putIfAbsent(key, value) }
      return result
    }
  }

  private class NamedListAccessor<T : VueNamedSymbol>(extInfoAccessor: (VueContainerInfo) -> List<T>)
    : ListAccessor<T>(extInfoAccessor) {

    override fun keyExtractor(obj: T): Any {
      return obj.name
    }
  }

  private class ModelAccessor(extInfoAccessor: (VueContainerInfo) -> VueModelDirectiveProperties?)
    : MemberAccessor<VueModelDirectiveProperties>(extInfoAccessor) {

    override fun empty(): VueModelDirectiveProperties {
      return VueModelDirectiveProperties()
    }

    override fun merge(arg1: VueModelDirectiveProperties, arg2: VueModelDirectiveProperties): VueModelDirectiveProperties {
      return arg2
    }
  }

  private class TemplateAccessor(extInfoAccessor: (VueContainerInfo) -> VueTemplate<*>?)
    : MemberAccessor<VueTemplate<*>?>(extInfoAccessor, true) {

    override fun empty(): VueTemplate<*>? {
      return null
    }
  }

  private class DelimitersAccessor(extInfoAccessor: (VueContainerInfo) -> Pair<String, String>?)
    : MemberAccessor<Pair<String, String>?>(extInfoAccessor, true) {

    override fun empty(): Pair<String, String>? {
      return null
    }
  }

}

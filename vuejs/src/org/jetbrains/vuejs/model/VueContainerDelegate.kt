// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType

abstract class VueContainerDelegate(val delegate: VueRegularComponent) : VueContainer {

  override val parents: List<VueEntitiesContainer>
    get() = delegate.parents
  override val data: List<VueDataProperty>
    get() = delegate.data
  override val computed: List<VueComputedProperty>
    get() = delegate.computed
  override val methods: List<VueMethod>
    get() = delegate.methods
  override val props: List<VueInputProperty>
    get() = delegate.props
  override val emits: List<VueEmitCall>
    get() = delegate.emits
  override val slots: List<VueSlot>
    get() = delegate.slots
  override val extends: List<VueContainer>
    get() = delegate.extends
  override val model: VueModelDirectiveProperties
    get() = delegate.model
  override val components: Map<String, VueComponent>
    get() = delegate.components
  override val directives: Map<String, VueDirective>
    get() = delegate.directives
  override val filters: Map<String, VueFilter>
    get() = delegate.filters
  override val mixins: List<VueMixin>
    get() = delegate.mixins
  override val template: VueTemplate<*>?
    get() = delegate.template
  override val element: String?
    get() = delegate.element
  override val delimiters: Pair<String, String>?
    get() = delegate.delimiters
  override val thisType: JSType
    get() = delegate.thisType
  override val global: VueGlobal?
    get() = delegate.global

  override fun equals(other: Any?): Boolean =
    other === this
    || other?.javaClass == this.javaClass
    && other is VueContainerDelegate
    && other.delegate == delegate

  override fun hashCode(): Int =
    delegate.hashCode()

}
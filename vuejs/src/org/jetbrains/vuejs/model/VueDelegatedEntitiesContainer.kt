// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.openapi.util.UserDataHolderBase

abstract class VueDelegatedEntitiesContainer<T : VueEntitiesContainer> : UserDataHolderBase(), VueEntitiesContainer {

  protected abstract val delegate: T

  override val components: Map<String, VueComponent> get() = delegate.components
  override val directives: Map<String, VueDirective> get() = delegate.directives
  override val filters: Map<String, VueFilter> get() = delegate.filters
  override val mixins: List<VueMixin> get() = delegate.mixins
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer

interface VueEntitiesContainer : VueScopeElement, VueInstanceOwner {
  fun createPointer(): Pointer<out VueEntitiesContainer>

  val components: Map<String, VueComponent>
  val directives: Map<String, VueDirective>
  val filters: Map<String, VueFilter>
  val mixins: List<VueMixin>
}

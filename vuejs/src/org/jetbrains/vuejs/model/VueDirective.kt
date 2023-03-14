// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer

interface VueDirective : VueNamedEntity, VueScopeElement {
  val jsType: JSType? get() = null
  val modifiers: List<VueDirectiveModifier> get() = emptyList()
  val argument: VueDirectiveArgument? get() = null

  fun createPointer(): Pointer<VueDirective>
}

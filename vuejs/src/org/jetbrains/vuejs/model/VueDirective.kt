// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem

interface VueDirective : VueNamedEntity, VueScopeElement, VueDocumentedItem {
  val acceptsNoValue: Boolean get() = true
  val acceptsValue: Boolean get() = true
  val jsType: JSType? get() = null
  val modifiers: List<VueDirectiveModifier> get() = emptyList()
  val argument: VueDirectiveArgument? get() = null
}

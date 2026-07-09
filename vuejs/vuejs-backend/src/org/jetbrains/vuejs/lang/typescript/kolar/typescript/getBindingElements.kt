// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import com.intellij.lang.javascript.psi.JSDestructuringContainer
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiElement

fun JSDestructuringContainer.getBindingElements(): List<BindingElement> =
  TODO("not implemented")

interface BindingElement {
  val source: PsiElement

  val nameIdentifier: PsiElement
  val isRest: Boolean
  val initializer: JSExpression?
}

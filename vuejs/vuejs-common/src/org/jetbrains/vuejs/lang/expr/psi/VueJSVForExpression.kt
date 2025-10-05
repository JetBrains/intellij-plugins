// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.psi.PsiElement

interface VueJSVForExpression : JSExpression {
  fun getVarStatement(): JSVarStatement?
  fun getReferenceExpression(): PsiElement?
  fun getCollectionExpression(): JSExpression?
}
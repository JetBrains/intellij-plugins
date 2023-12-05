// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement

interface Angular2BlockParameter : Angular2EmbeddedExpression {

  override fun getName(): String?

  val nameElement: PsiElement?

  val expression: JSExpression?

  val variables: List<JSVariable>

}
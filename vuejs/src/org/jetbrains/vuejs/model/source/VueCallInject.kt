// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.contextOfType
import org.jetbrains.vuejs.model.VueInject
import org.jetbrains.vuejs.model.resolveInjectionSymbol
import org.jetbrains.vuejs.types.VueSourceProvideType


class VueCallInject(
  override val name: String,
  override val source: PsiElement,
  private val symbolSource: PsiNamedElement? = null
) : VueInject {

  override val injectionKey: PsiNamedElement?
    get() = resolveInjectionSymbol(symbolSource)

  override val defaultValue: JSType?
    get() = source.contextOfType<JSCallExpression>(true)
      ?.let { VueSourceProvideType.inferTypeFromCallExpr(it) }
}
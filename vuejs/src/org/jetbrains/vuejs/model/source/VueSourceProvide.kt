// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.jetbrains.vuejs.model.resolveInjectionSymbol
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.types.VueSourceProvideType

class VueSourceProvide(
  override val name: String,
  sourceElement: PsiElement,
  private val symbolSource: PsiNamedElement? = null
) : VueProvide {

  override val injectionKey: PsiNamedElement?
    get() = resolveInjectionSymbol(symbolSource)

  override val jsType: JSType = VueSourceProvideType(sourceElement, symbolSource)

  override val source: PsiElement = when (sourceElement) {
    is JSLiteralExpression -> VueImplicitElement(name, jsType, sourceElement, JSImplicitElement.Type.Property, true)
    else -> sourceElement
  }
}
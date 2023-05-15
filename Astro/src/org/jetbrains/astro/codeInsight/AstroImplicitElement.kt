// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.TypeScriptMergedTypeImplicitElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class AstroImplicitElement(name: String, jsType: JSType?, provider: PsiElement, kind: JSImplicitElement.Type,
                           private val equivalentToProvider: Boolean = false)
  : JSLocalImplicitElementImpl(name, jsType, provider, kind) {

  override fun getTextRange(): TextRange? {
    return myProvider!!.textRange
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean =
    when (another) {
      is AstroImplicitElement -> equals(another)
      is TypeScriptMergedTypeImplicitElement -> equals(another.explicitElement)
      else -> equivalentToProvider && this.myProvider!! == another
    }

  fun copyWithProvider(provider: PsiElement): AstroImplicitElement =
    AstroImplicitElement(name, jsType, provider, type, equivalentToProvider)
}

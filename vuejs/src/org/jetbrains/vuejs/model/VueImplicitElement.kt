// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.TypeScriptMergedTypeImplicitElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class VueImplicitElement(name: String, jsType: JSType?, provider: PsiElement, kind: JSImplicitElement.Type,
                         private val equivalentToProvider: Boolean = false)
  : JSLocalImplicitElementImpl(name, jsType, provider, kind) {

  override fun getTextRange(): TextRange? {
    return myProvider!!.textRange
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean =
    when (another) {
      is VueImplicitElement -> equals(another)
      is TypeScriptMergedTypeImplicitElement -> equals(another.explicitElement)
      else -> equivalentToProvider && this.myProvider!! == another
    }

  fun copyWithProvider(provider: PsiElement): VueImplicitElement =
    VueImplicitElement(name, jsType, provider, type, equivalentToProvider)
}

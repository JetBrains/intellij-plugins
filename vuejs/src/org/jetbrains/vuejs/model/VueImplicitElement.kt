// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import java.util.*

class VueImplicitElement(name: String, jsType: JSType?, provider: PsiElement, kind: JSImplicitElement.Type,
                         private val equivalentToProvider: Boolean = false)
  : JSLocalImplicitElementImpl(name, jsType, provider, kind) {

  override fun getTextRange(): TextRange? {
    return myProvider!!.textRange
  }

  override fun equals(other: Any?): Boolean {
    return (other is VueImplicitElement)
           && other.myName == myName
           && other.myProvider == myProvider
           && other.myKind == myKind
           && ((other.jsType == null && jsType == null)
               || (other.jsType?.isEquivalentTo(this.jsType, null) == true))

  }

  override fun isEquivalentTo(another: PsiElement?): Boolean {
    return equivalentToProvider && this.myProvider!! == another
  }

  override fun hashCode(): Int {
    return Objects.hash(javaClass, myName, myProvider, myKind)
  }

}

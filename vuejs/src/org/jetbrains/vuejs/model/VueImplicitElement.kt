// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

class VueImplicitElement(name: String, jsType: JSType?, provider: PsiElement, kind: JSImplicitElement.Type,
                         private val equivalentToProvider: Boolean = false)
  : JSLocalImplicitElementImpl(name, jsType, provider, kind) {

  override fun getTextRange(): TextRange? {
    return myProvider.textRange
  }

  override fun setName(name: String): PsiElement {
    if (myProvider is JSLiteralExpression) {
      val provider = myProvider as JSLiteralExpression
      val qNameStart = provider.text.indexOf(qualifiedName)
      if (qNameStart >= 0) {
        val oldName = getName()
        val range = TextRange.from(qNameStart + qualifiedName.length - oldName.length, oldName.length)
        ElementManipulators.handleContentChange(provider, range, name)
      }
      else {
        ElementManipulators.handleContentChange(provider, name)
      }
    }
    var parent: PsiElement? = myProvider
    while (parent != null && parent !is JSExecutionScope) {
      if (parent is PsiNamedElement && StringUtil.equals(getName(), parent.name)) {
        parent.setName(name)
        break
      }
      parent = parent.parent
    }
    return VueImplicitElement(name, jsType, myProvider, myKind!!, equivalentToProvider)
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean =
    when (another) {
      is VueImplicitElement -> equals(another)
      is JSImplicitElement -> equals(another.parent)
      else -> equivalentToProvider && this.myProvider == another
    }

  fun copyWithProvider(provider: PsiElement): VueImplicitElement =
    VueImplicitElement(name, jsType, provider, type, equivalentToProvider)
}

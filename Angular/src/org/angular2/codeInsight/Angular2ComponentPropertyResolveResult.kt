// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.psi.PsiElement

class Angular2ComponentPropertyResolveResult(element: PsiElement,
                                             private val myPropertySignature: JSRecordType.PropertySignature) : JSResolveResult(element) {

  val jsType: JSType?
    get() = myPropertySignature.jsTypeWithOptionality

  fun copyWith(element: PsiElement): Angular2ComponentPropertyResolveResult {
    return Angular2ComponentPropertyResolveResult(element, myPropertySignature)
  }

  override fun toString(): String {
    return "Angular2ComponentPropertyResolveResult{myElement=$element, myType=$jsType}"
  }
}

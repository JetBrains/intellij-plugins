// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely

class AstroConfigAccessibilityChecker : TypeScriptConfigAccessibilityChecker() {

  override fun checkImpl(place: PsiElement?, element: PsiElement): JSResolveResult.ProblemKind? {
    if (place.asSafely<JSReferenceExpression>()
        ?.takeIf { it.qualifier == null }
        ?.referenceName == ASTRO_IMPLICIT_OBJECT)
      return null
    return super.checkImpl(place, element)
  }
}

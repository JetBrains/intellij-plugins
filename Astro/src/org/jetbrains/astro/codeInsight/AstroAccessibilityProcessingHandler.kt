// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler
import com.intellij.lang.javascript.psi.resolve.accessibility.JSAccessibilityChecker
import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker
import com.intellij.psi.PsiElement

class AstroAccessibilityProcessingHandler(place: PsiElement?) : AccessibilityProcessingHandler(place) {

  override fun getCheckers(): Collection<JSAccessibilityChecker> {
    return ourCheckers
  }

  companion object {

    private val ourCheckers: List<JSAccessibilityChecker> =
      CHECKERS.filter { checker -> checker !is TypeScriptConfigAccessibilityChecker } + AstroConfigAccessibilityChecker()

  }
}

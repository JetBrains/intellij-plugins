// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler
import com.intellij.lang.javascript.psi.resolve.accessibility.JSAccessibilityChecker
import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker
import com.intellij.psi.PsiElement

class Angular2AccessibilityProcessingHandler(_place: PsiElement?) : AccessibilityProcessingHandler(_place) {

  override fun getCheckers(): Collection<JSAccessibilityChecker> {
    return ourCheckers
  }

  companion object {

    private val ourCheckers: List<JSAccessibilityChecker> =
      CHECKERS.filter { checker -> checker !is TypeScriptConfigAccessibilityChecker } + Angular2ConfigAccessibilityChecker()

  }
}

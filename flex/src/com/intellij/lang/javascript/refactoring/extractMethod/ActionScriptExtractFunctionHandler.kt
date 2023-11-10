// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.refactoring.extractMethod

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.openapi.ui.DialogWrapper

class ActionScriptExtractFunctionHandler : JSExtractFunctionHandler() {
  override fun showDialog(
    ci: ContextInfo,
    signatureGenerator: ExtractedFunctionSignatureGenerator,
    scope: IntroductionScope,
    occurrences: Array<out JSExpression>,
  ): JSExtractFunctionSettings? {
    val dialog = ActionScriptExtractFunctionDialog(signatureGenerator, ci, scope, occurrences)
    dialog.show()

    if (dialog.exitCode != DialogWrapper.OK_EXIT_CODE) {
      return null
    }

    return dialog
  }
}

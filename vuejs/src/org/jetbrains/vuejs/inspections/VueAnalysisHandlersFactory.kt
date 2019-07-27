// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.highlighting.JSFixFactory
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSReferenceInspectionProblemReporter
import com.intellij.psi.ResolveResult

class VueAnalysisHandlersFactory : JSAnalysisHandlersFactory() {
  override fun getReferenceChecker(reporter: JSReferenceInspectionProblemReporter): JSReferenceChecker {
    return object : JSReferenceChecker((reporter)) {
      override fun addCreateFromUsageFixes(node: JSReferenceExpression?,
                                           resolveResults: Array<out ResolveResult>?,
                                           fixes: MutableList<LocalQuickFix>?,
                                           inTypeContext: Boolean,
                                           ecma: Boolean): Boolean {
        return inTypeContext
      }

      override fun addCreateFromUsageFixesForCall(referenceExpression: JSReferenceExpression,
                                                  isNewExpression: Boolean,
                                                  resolveResults: Array<out ResolveResult>,
                                                  quickFixes: MutableList<LocalQuickFix>) {
        quickFixes.add(JSFixFactory.getInstance().renameReferenceFix())
      }
    }
  }
}

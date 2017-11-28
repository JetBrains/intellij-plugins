package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.highlighting.JSFixFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSReferenceInspectionProblemReporter
import com.intellij.psi.ResolveResult

/**
 * @author Irina.Chernushina on 11/7/2017.
 */
class VueAnalysisHandlersFactory : JSAnalysisHandlersFactory() {
  override fun getReferenceChecker(reporter: JSReferenceInspectionProblemReporter): JSReferenceChecker {
    return object: JSReferenceChecker((reporter)) {
      override fun addCreateFromUsageFixes(node: JSReferenceExpression?,
                                           resolveResults: Array<out ResolveResult>?,
                                           fixes: MutableList<LocalQuickFix>?,
                                           inTypeContext: Boolean,
                                           ecma: Boolean): Boolean {
        return inTypeContext
      }

      override fun addCreateFromUsageFixesForCall(node: JSCallExpression,
                                                  referenceExpression: JSReferenceExpression,
                                                  resolveResults: Array<out ResolveResult>,
                                                  quickFixes: MutableList<LocalQuickFix>) {
        quickFixes.add(JSFixFactory.getInstance().renameReferenceFix())
      }
    }
  }
}
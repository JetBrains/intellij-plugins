package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.highlighting.JSFixFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult

/**
 * @author Irina.Chernushina on 11/7/2017.
 */
class VueAnalysisHandlersFactory : JSAnalysisHandlersFactory() {
  override fun createAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder): JSAnnotatingVisitor {
    return object: JSAnnotatingVisitor(psiElement, holder) {
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
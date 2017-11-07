package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor
import com.intellij.psi.PsiElement

/**
 * @author Irina.Chernushina on 11/7/2017.
 */
class VueAnalysisHandlersFactory : JSAnalysisHandlersFactory() {
  override fun createAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder): JSAnnotatingVisitor {
    return object: JSAnnotatingVisitor(psiElement, holder) {
      override fun suggestCreateVarFromUsage(node: JSReferenceExpression?): Boolean {
        return false
      }
    }
  }
}
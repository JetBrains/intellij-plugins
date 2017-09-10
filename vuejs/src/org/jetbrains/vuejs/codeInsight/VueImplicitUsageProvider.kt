package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.language.VueVForExpression

/**
 * @author Irina.Chernushina on 8/21/2017.
 */
class VueImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement?): Boolean {
    if (element is JSVariable) {
      val vForExpression = PsiTreeUtil.getParentOfType(element, VueVForExpression::class.java)
      return vForExpression?.getVarStatement()?.variables?.contains(element) ?: return false
    }
    return false
  }

  override fun isImplicitRead(element: PsiElement?): Boolean = false

  override fun isImplicitWrite(element: PsiElement?): Boolean = false
}
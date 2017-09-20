package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.language.VueVForExpression

/**
 * @author Irina.Chernushina on 8/21/2017.
 */
class VueImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement?): Boolean {
    if (element is JSVariable) {
      val vForExpression = PsiTreeUtil.getParentOfType(element, VueVForExpression::class.java)
      val contains = vForExpression?.getVarStatement()?.variables?.contains(element)
      if (contains != null && contains) {
        val tag = PsiTreeUtil.getParentOfType(vForExpression, XmlTag::class.java)
        if (tag != null) {
          val result = ReferencesSearch.search(element, LocalSearchScope(tag)).find {
            it is PsiElement && !JSResolveUtil.isSelfReference(it)
          }
          return result != null
        }
      }
      return false
    }
    return false
  }

  override fun isImplicitRead(element: PsiElement?): Boolean = false

  override fun isImplicitWrite(element: PsiElement?): Boolean = false
}
package org.angular2.codeInsight

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.Angular2ExprDialect

/**
 * @author Dennis.Ushakov
 */
class Angular2ResolveScopeProvider : JSElementResolveScopeProvider {

  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    if (DialectDetector.languageDialectOfElement(element) is Angular2ExprDialect) {
      val clazz = Angular2EntitiesProvider.findTemplateComponent(element)?.jsResolveScope
      if (clazz != null) {
        return JSResolveUtil.getResolveScope(clazz)
      }
    }
    return null
  }
}

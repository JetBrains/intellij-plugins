package org.angular2.css.findUsages

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2LangUtil

class Angular2CssClassOrIdUsagesProvider : CssClassOrIdReferenceBasedUsagesProvider() {

  override fun acceptElement(candidate: PsiElement): Boolean {
    return (candidate is XmlAttribute || candidate is JSLiteralExpression || candidate is JSProperty)
           && Angular2LangUtil.isAngular2Context(candidate)
  }
}
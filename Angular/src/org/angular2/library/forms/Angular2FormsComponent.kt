package org.angular2.library.forms

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.library.forms.impl.Angular2FormsComponentImpl

interface Angular2FormsComponent {

  fun getFormGroupFor(reference: JSReferenceExpression): Angular2FormGroup?

  companion object {
    fun getFor(location: PsiElement): Angular2FormsComponent? =
      if (location.language is Angular2HtmlDialect || location.language is Angular2Language)
        Angular2SourceUtil.findComponentClass(location)?.let { Angular2FormsComponentImpl(it) }
      else
        location.parentOfType<TypeScriptClass>(true)
          ?.takeIf { Angular2EntitiesProvider.getComponent(it) != null }
          ?.let { Angular2FormsComponentImpl(it) }
  }
}
package org.angular2.intentions

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.intentions.JSIntroduceVariableIntention
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.openapi.editor.SelectionModel
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.Angular2HtmlDialect

class AngularIntroduceLetVariableIntention : JSIntroduceVariableIntention() {

  init {
    text = Angular2Bundle.message("angular.intention.introduce.let.variable.name")
  }

  override fun createIntroduceVariableHandler(): AngularIntroduceLetVariableHandler =
    AngularIntroduceLetVariableHandler()

  override fun isGenerallyApplicable(selectionModel: SelectionModel, expression: JSExpression?): Boolean =
    if (expression?.containingFile?.language?.asSafely<Angular2HtmlDialect>()?.templateSyntax?.enableLetSyntax != true)
      false
    else
      super.isGenerallyApplicable(selectionModel, expression)

  override fun hasAcceptableStatementExecutionScope(element: PsiElement): Boolean =
    PsiTreeUtil.findFirstParent(element, true) { el: PsiElement? ->
      if (el !is JSExecutionScope) return@findFirstParent false
      if (el !is JSEmbeddedContent || isSupportedEmbeddedExpression(el)) return@findFirstParent true
      ES6PsiUtil.isEmbeddedBlock(el)
    } != null

  private fun isSupportedEmbeddedExpression(el: JSEmbeddedContent): Boolean =
    el is Angular2EmbeddedExpression
    && el !is Angular2TemplateBindings
    && (el !is Angular2BlockParameter || el.isPrimaryExpression)

}
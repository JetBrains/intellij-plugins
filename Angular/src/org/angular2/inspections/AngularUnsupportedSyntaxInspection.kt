package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSExpressionWithOperationNode
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.AngularVersion
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.lexer.Angular2TokenTypes.Companion.ASSIGNMENT_OPERATORS

class AngularUnsupportedSyntaxInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JSElementVisitor() {
      override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element is LeafPsiElement
            && element.parent?.let { it.language is Angular2ExprDialect && it is JSExpressionWithOperationNode } == true
        ) {
          val version = keywordToVersionMap[element.elementType]
          if (version != null
              && Angular2LangUtil.isAngular2Context(element)
              && !Angular2LangUtil.isAtLeastAngularVersion(element, version)
          ) {
            holder.registerProblem(element, Angular2Bundle.htmlMessage(
              "angular.inspection.unsupported-syntax-inspection.message.operator-ng-or-above",
              element.text.withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, element),
              version.toString().removePrefix("V_").replace("_", "."),
            ))
          }
        }
        else if (
          element is JSStringTemplateExpression
          && element.language is Angular2ExprDialect
          && Angular2LangUtil.isAngular2Context(element)
          && !Angular2LangUtil.isAtLeastAngularVersion(element, AngularVersion.V_19_2)
        ) {
          holder.registerProblem(element, Angular2Bundle.htmlMessage("angular.inspection.unsupported-syntax-inspection.message.template"))
        }
      }
    }


}

private val keywordToVersionMap = mutableMapOf(
  JSTokenTypes.TYPEOF_KEYWORD to AngularVersion.V_19,
  JSTokenTypes.IN_KEYWORD to AngularVersion.V_20,
  JSTokenTypes.MULTMULT to AngularVersion.V_20,
).apply {
  ASSIGNMENT_OPERATORS.types.forEach { if (it != JSTokenTypes.EQ) put(it, AngularVersion.V_20_1) }
}
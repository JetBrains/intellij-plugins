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
import com.intellij.psi.util.parentOfType
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.blocks.BLOCK_ELSE_IF
import org.angular2.codeInsight.blocks.PARAMETER_AS
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.AngularVersion
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.lexer.Angular2TokenTypes.Companion.ASSIGNMENT_OPERATORS
import org.angular2.lang.html.psi.Angular2HtmlBlock

class AngularUnsupportedSyntaxInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JSElementVisitor() {
      override fun visitElement(element: PsiElement) {
        super.visitElement(element)
         if (element is LeafPsiElement) {
          if (element.parent?.let { it.language is Angular2ExprDialect && it is JSExpressionWithOperationNode } == true) {
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
          else if (element.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME
                   && element.text == PARAMETER_AS
                   && element.parentOfType<Angular2HtmlBlock>()?.name == BLOCK_ELSE_IF
                   && !Angular2LangUtil.isAtLeastAngularVersion(element, AngularVersion.V_20_2)) {
            holder.registerProblem(element, Angular2Bundle.htmlMessage(
              "angular.inspection.unsupported-syntax-inspection.message.else-if-as-alias",
              PARAMETER_AS.withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, element),
              "@${BLOCK_ELSE_IF}".withColor(Angular2HighlightingUtils.TextAttributesKind.NG_BLOCK, element),
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
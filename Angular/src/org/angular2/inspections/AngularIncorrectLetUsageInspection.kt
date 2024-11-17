package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElementVisitor
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.codeInsight.blocks.isLetDeclarationVariable
import org.angular2.codeInsight.blocks.isLetReferenceBeforeDeclaration
import org.angular2.codeInsight.template.getTemplateElementsScopeFor
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language

class AngularIncorrectLetUsageInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JSElementVisitor() {
      override fun visitJSVariable(node: JSVariable) {
        if (!isLetDeclarationVariable(node) || node.language != Angular2Language) return
        val varName = node.name
                      ?: return
        val varScope = getTemplateElementsScopeFor(node)
                       ?: return
        var hasConflicts = false
        varScope.resolve { resolveResult ->
          val element = resolveResult.element as? JSPsiElementBase
          if (resolveResult.isValidResult
              && element != null
              && element != node
              && varName == element.name) {
            hasConflicts = true
          }
        }
        if (hasConflicts) {
          holder.registerProblem(
            node, Angular2Bundle.htmlMessage("angular.inspection.incorrect-let-usage.message.conflicting-declaration",
                                             "@${BLOCK_LET}".withColor(TextAttributesKind.NG_BLOCK, node),
                                             varName.withColor(TextAttributesKind.TS_LOCAL_VARIABLE, node)))
        }
        super.visitJSVariable(node)
      }

      override fun visitJSReferenceExpression(node: JSReferenceExpression) {
        if (node.qualifier == null && node.language == Angular2Language) {
          val resolved = node.resolve()
          if (resolved != null && isLetReferenceBeforeDeclaration(node, resolved)) {
            holder.registerProblem(
              node, Angular2Bundle.htmlMessage("angular.inspection.incorrect-let-usage.message.used-before-declaration",
                                               "@${BLOCK_LET}".withColor(TextAttributesKind.NG_BLOCK, node),
                                               (node.referenceName ?: "").withColor(TextAttributesKind.TS_LOCAL_VARIABLE, node)))
          }
        }
      }

    }
}
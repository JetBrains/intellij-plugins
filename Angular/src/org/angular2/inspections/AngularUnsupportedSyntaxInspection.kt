package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.Angular2Language

class AngularUnsupportedSyntaxInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JSElementVisitor() {
      override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element is LeafPsiElement
            && element.parent?.language is Angular2Language
            && element.elementType == JSTokenTypes.TYPEOF_KEYWORD
            && Angular2LangUtil.isAngular2Context(element)
            && !Angular2LangUtil.isAtLeastAngularVersion(element, Angular2LangUtil.AngularVersion.V_19)
        ) {
          holder.registerProblem(element, Angular2Bundle.htmlMessage(
            "angular.inspection.unsupported-syntax-inspection.message.ng19-or-above",
            "typeof".withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, element)))
        }
      }
    }


}
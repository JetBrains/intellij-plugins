package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElementVisitor
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.inspections.quickfixes.CallSignalQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.signals.Angular2SignalUtils

class AngularUncalledSignalLengthPropertyAccessInspection : JSInspection() {

  override fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor =
    if (Angular2LangUtil.isAngular2Context(holder.file))
      object : JSElementVisitor() {
        override fun visitJSReferenceExpression(node: JSReferenceExpression) {
          if (node.referenceName == "length" && node.qualifier?.let { Angular2SignalUtils.isSignal(it, it) } == true) {
            node.referenceNameElement?.let {
              holder.registerProblem(
                it,
                Angular2Bundle.htmlMessage("angular.inspection.uncalled-signal-length-property-access.message",
                                       "length".withColor(Angular2HighlightingUtils.TextAttributesKind.TS_PROPERTY, node)),
                CallSignalQuickFix()
              )
            }
          }
        }
      }
    else
      PsiElementVisitor.EMPTY_VISITOR
}
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import org.angular2.lang.Angular2Bundle

class CallSignalQuickFix : LocalQuickFix {
  override fun getFamilyName(): @IntentionFamilyName String =
    Angular2Bundle.message("angular.inspection.uncalled-signal-length-property-access.quick-fix.message")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val referenceNameElement = descriptor.psiElement
    val reference = referenceNameElement.parent as? JSReferenceExpression
    val qualifier = reference?.qualifier ?: return

    val callExpression =
      JSChangeUtil.createExpressionWithContext(qualifier.text + "()", reference)
        ?.psi?.asSafely<JSCallExpression>()
      ?: return
    qualifier.replace(callExpression)
  }
}
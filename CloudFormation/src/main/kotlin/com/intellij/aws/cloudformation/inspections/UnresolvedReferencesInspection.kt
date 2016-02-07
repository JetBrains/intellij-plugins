package com.intellij.aws.cloudformation.inspections

import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElementVisitor

class UnresolvedReferencesInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    if (!CloudFormationPsiUtils.isCloudFormationFile(session.file)) {
      return super.buildVisitor(holder, isOnTheFly, session)
    }

    return object : JsonElementVisitor() {
      override fun visitStringLiteral(o: JsonStringLiteral) {
        for (reference in o.references) {
          if (reference is CloudFormationReferenceBase) {
            val element = reference.resolve()
            if (element == null) {
              holder.registerProblem(reference)
            }
          }
        }
      }
    }
  }
}

package com.intellij.aws.cloudformation.inspections

import com.intellij.aws.cloudformation.CloudFormationFormatChecker
import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import com.intellij.util.ArrayUtil

class FormatViolationInspection : LocalInspectionTool() {
  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (!CloudFormationPsiUtils.isCloudFormationFile(file)) {
      return null
    }

    val checker = CloudFormationFormatChecker(manager, isOnTheFly)
    checker.file(file)
    return ArrayUtil.toObjectArray(checker.problems, ProblemDescriptor::class.java)
  }
}

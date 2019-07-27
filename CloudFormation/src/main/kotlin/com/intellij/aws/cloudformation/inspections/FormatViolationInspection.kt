package com.intellij.aws.cloudformation.inspections

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile


abstract class FormatViolationInspection : LocalInspectionTool() {
  override fun runForWholeFile(): Boolean = true

  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (!CloudFormationPsiUtils.isCloudFormationFile(file)) {
      return null
    }

    val parsed = CloudFormationParser.parse(file)
    val inspected = CloudFormationInspections.inspectFile(parsed)

    val problems = parsed.problems.plus(inspected.problems).map {
      manager.createProblemDescriptor(
          it.element,
          it.description,
          isOnTheFly,
          LocalQuickFix.EMPTY_ARRAY,
          ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }

    return problems.toTypedArray()
  }
}

class JsonFormatViolationInspection: FormatViolationInspection()
class YamlFormatViolationInspection: FormatViolationInspection()

package com.intellij.aws.cloudformation.inspections

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder.unresolvedReferenceMessage
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLCompoundValue

abstract class UnresolvedReferencesInspection : LocalInspectionTool() {
  override fun runForWholeFile(): Boolean = true
  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (!CloudFormationPsiUtils.isCloudFormationFile(file)) {
      return super.checkFile(file, manager, isOnTheFly)
    }

    val parsed = CloudFormationParser.parse(file)
    val inspectionResult = CloudFormationInspections.inspectFile(parsed)

    return inspectionResult.references.entries().mapNotNull {
      val reference = it.value

      val element = reference.resolve()
      if (element != null) return@mapNotNull null

      if (reference.element is YAMLCompoundValue) {
        // Do not handle special cases like double string literals
        // IDEA fails to create reference here with Cannot find manipulator for YAML compound value in YAML compound value:null class class com.intellij.aws.cloudformation.references.CloudFormationEntityReference
        return@mapNotNull null
      }

      return@mapNotNull manager.createProblemDescriptor(reference.element, reference.rangeInElement,
          unresolvedReferenceMessage(reference), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, isOnTheFly)
    }.toTypedArray()
  }

  override fun getStaticDescription(): String? = ""
}

class JsonUnresolvedReferencesInspection: UnresolvedReferencesInspection()
class YamlUnresolvedReferencesInspection: UnresolvedReferencesInspection()

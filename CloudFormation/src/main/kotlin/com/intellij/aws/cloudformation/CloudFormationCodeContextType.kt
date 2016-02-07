package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.json.liveTemplates.JsonContextType
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NonNls

class CloudFormationCodeContextType : TemplateContextType(CloudFormationCodeContextType.CLOUDFORMATION_TEMPLATE, CloudFormationBundle.getString("aws.cloudformation.template.context.type"), JsonContextType::class.java) {

  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return CloudFormationPsiUtils.isCloudFormationFile(file)
  }

  companion object {
    @NonNls
    private val CLOUDFORMATION_TEMPLATE = "AWS_CLOUD_FORMATION"
  }
}

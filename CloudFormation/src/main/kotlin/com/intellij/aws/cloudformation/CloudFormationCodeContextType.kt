package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.json.liveTemplates.JsonContextType
import com.intellij.psi.PsiFile

class CloudFormationCodeContextType : TemplateContextType(
    "AWS_CLOUD_FORMATION",
    CloudFormationBundle.getString("aws.cloudformation.template.context.type"),
    JsonContextType::class.java) {

  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return CloudFormationPsiUtils.isCloudFormationFile(file)
  }
}

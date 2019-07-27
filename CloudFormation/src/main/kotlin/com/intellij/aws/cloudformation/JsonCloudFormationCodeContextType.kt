package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.json.liveTemplates.JsonContextType
import com.intellij.psi.PsiFile

class JsonCloudFormationCodeContextType : TemplateContextType(
    "AWS_CLOUD_FORMATION_JSON",
    CloudFormationBundle.getString("aws.cloudformation.template.context.type.json"),
    JsonContextType::class.java) {

  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    val fileType = file.viewProvider.fileType
    return fileType === JsonCloudFormationFileType.INSTANCE
  }
}

package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile

class YamlCloudFormationCodeContextType : TemplateContextType(CloudFormationBundle.message("aws.cloudformation.template.context.type.yaml")) {

  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    val fileType = file.viewProvider.fileType
    return fileType === YamlCloudFormationFileType.INSTANCE
  }
}

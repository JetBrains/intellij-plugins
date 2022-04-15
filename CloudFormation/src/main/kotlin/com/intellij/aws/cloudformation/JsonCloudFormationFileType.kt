package com.intellij.aws.cloudformation

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class JsonCloudFormationFileType private constructor(): LanguageFileType(JsonLanguage.INSTANCE, true) {
  override fun getName(): String = "AWSCloudFormation (JSON)"
  override fun getDescription(): String = CloudFormationBundle.message("filetype.aws.cloudformation.templates.json.description")
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon = CloudFormationIcons.AwsFile
  override fun getDisplayName(): String = CloudFormationBundle.message("filetype.aws.cloudformation.templates.json.display.name")

  companion object {
    @JvmField
    val INSTANCE: LanguageFileType = JsonCloudFormationFileType()
  }
}

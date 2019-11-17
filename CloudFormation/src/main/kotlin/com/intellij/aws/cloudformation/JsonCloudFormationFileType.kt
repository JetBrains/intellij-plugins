package com.intellij.aws.cloudformation

import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class JsonCloudFormationFileType : LanguageFileType(JsonLanguage.INSTANCE) {
  override fun getName(): String = "AWSCloudFormation (JSON)"
  override fun getDescription(): String = "AWS CloudFormation templates (JSON)"
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.FileTypes.Json

  companion object {
    @JvmField
    val INSTANCE = JsonCloudFormationFileType()
  }
}

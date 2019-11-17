package com.intellij.aws.cloudformation

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class YamlCloudFormationFileType : LanguageFileType(YAMLLanguage.INSTANCE) {
  override fun getName(): String = "AWSCloudFormation (YAML)"
  override fun getDescription(): String = "AWS CloudFormation templates (YAML)"
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.FileTypes.Yaml

  companion object {
    @JvmField
    val INSTANCE = YamlCloudFormationFileType()
  }
}

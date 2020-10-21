package com.intellij.aws.cloudformation

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class YamlCloudFormationFileType private constructor(): LanguageFileType(YAMLLanguage.INSTANCE, true) {
  override fun getName(): String = "AWSCloudFormation (YAML)"
  override fun getDescription(): String = CloudFormationBundle.message("label.aws.cloudformation.templates.yaml")
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.FileTypes.Yaml

  companion object {
    @JvmField
    val INSTANCE = YamlCloudFormationFileType()
  }
}

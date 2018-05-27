package com.intellij.aws.cloudformation

import com.google.common.base.Charsets
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.YAMLLanguage
import javax.swing.Icon

class YamlCloudFormationFileType : LanguageFileType(YAMLLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {
  private val fileTypeRecursionGuard = RecursionManager.createGuard(javaClass.simpleName)

  override fun getName(): String = "AWSCloudFormation (YAML)"
  override fun getDescription(): String = "AWS CloudFormation templates (YAML)"
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.Nodes.DataTables

  override fun isMyFileType(file: VirtualFile): Boolean {
    val extension = file.extension ?: return false

    return fileTypeRecursionGuard.doPreventingRecursion(javaClass, false, {
      if (YamlCloudFormationFileType.EXTENSION1.equals(extension, ignoreCase = true) ||
          YamlCloudFormationFileType.EXTENSION2.equals(extension, ignoreCase = true) ||
          FileTypeManager.getInstance().getFileTypeByFile(file) === YAMLFileType.YML) {
        return@doPreventingRecursion signatureDetector.detectSignature(file)
      }

      return@doPreventingRecursion false
    }) ?: false
  }

  companion object {
    val INSTANCE = YamlCloudFormationFileType()

    private val EXTENSION1 = "yml"
    private val EXTENSION2 = "yaml"

    private val signatureDetector = FileContentDetector(
        JsonCloudFormationFileType.INSTANCE.name,
        false,
        CloudFormationSection.FormatVersion.id,
        CloudFormationConstants.awsServerless20161031TransformName)
  }
}

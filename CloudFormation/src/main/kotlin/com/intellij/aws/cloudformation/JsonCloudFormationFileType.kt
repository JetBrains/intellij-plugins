package com.intellij.aws.cloudformation

import com.intellij.icons.AllIcons
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.yaml.YAMLFileType
import javax.swing.Icon

class JsonCloudFormationFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {
  override fun getName(): String = "AWSCloudFormation (JSON)"
  override fun getDescription(): String = "AWS CloudFormation templates (JSON)"
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.FileTypes.Json

  override fun isMyFileType(file: VirtualFile): Boolean {
    val extension = file.extension ?: return false

    return RecursionManager.doPreventingRecursion(javaClass, false) {
      val fileTypeByFile = FileTypeManager.getInstance().getFileTypeByFile(file)

      if (fileTypeByFile === YamlCloudFormationFileType.INSTANCE ||
          fileTypeByFile === YAMLFileType.YML)
        return@doPreventingRecursion false

      if (JsonFileType.DEFAULT_EXTENSION.equals(extension, ignoreCase = true) ||
          EXTENSION.equals(extension, ignoreCase = true) ||
          fileTypeByFile === JsonFileType.INSTANCE) {
        return@doPreventingRecursion signatureDetector.detectSignature(file)
      }

      return@doPreventingRecursion false
    } ?: false
  }

  companion object {
    @JvmField
    val INSTANCE = JsonCloudFormationFileType()

    private const val EXTENSION = "template"

    private val signatureDetector = FileContentDetector(
        INSTANCE.name,
        true,
        CloudFormationSection.FormatVersion.id)
  }
}

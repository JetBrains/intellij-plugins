package com.intellij.aws.cloudformation

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class CloudFormationFileTypeDetector: FileTypeRegistry.FileTypeDetector {
  override fun getVersion(): Int = 1

  override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
    if (file.extension != "template") return null
    if (firstCharsIfText == null) return null

    if (isJson(firstCharsIfText)) {
      return JsonCloudFormationFileType.INSTANCE
    }

    if (isYaml(firstCharsIfText)) {
      return YamlCloudFormationFileType.INSTANCE
    }

    return null
  }

  companion object {
    fun isYaml(chars: CharSequence): Boolean =
        chars.contains(CloudFormationSection.FormatVersion.id) ||
            chars.contains(CloudFormationConstants.awsServerless20161031TransformName)

    fun isJson(chars: CharSequence): Boolean {
      if (!chars.contains(CloudFormationSection.FormatVersion.id)) return false

      for (c in chars) {
        if (c == '{') return true
        if (!c.isWhitespace()) return false
      }

      // All whitespaces
      return false
    }
  }
}

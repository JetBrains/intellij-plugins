package com.intellij.aws.cloudformation

import com.google.common.base.Charsets
import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.FileSystemInterface
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile
import org.apache.commons.lang.ArrayUtils
import java.io.IOException
import java.util.*
import javax.swing.Icon

class CloudFormationFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {
  override fun getName(): String {
    return "AWSCloudFormation"
  }

  override fun getDescription(): String {
    return "AWS CloudFormation templates"
  }

  override fun getDefaultExtension(): String {
    return ""
  }

  override fun getIcon(): Icon? {
    return AllIcons.FileTypes.Json
  }

  override fun isMyFileType(file: VirtualFile): Boolean {
    return CloudFormationFileType.EXTENSION.equals(file.extension, ignoreCase = true) && detectFromContent(file)
  }

  private fun detectFromContent(file: VirtualFile): Boolean {
    if (file is StubVirtualFile) {
      // Helps New -> File get correct file type
      return true
    }

    val virtualFileSystem: VirtualFileSystem
    try {
      virtualFileSystem = file.fileSystem
    } catch (ignored: UnsupportedOperationException) {
      return false
    }

    if (virtualFileSystem !is FileSystemInterface) {
      return false
    }

    try {
      val inputStream = virtualFileSystem.getInputStream(file)
      try {
        val bytes = ByteArray(1024)

        val n = inputStream.read(bytes, 0, bytes.size)
        return n > 0 && findArray(bytes, BYTES_TO_DETECT_CFN_FILE) >= 0
      } finally {
        inputStream.close()
      }
    } catch (ignored: IOException) {
      return false
    }

  }

  companion object {
    val INSTANCE = CloudFormationFileType()

    private val EXTENSION = "template"
    private val BYTES_TO_DETECT_CFN_FILE = CloudFormationSections.FormatVersion.toByteArray(Charsets.US_ASCII)

    private fun findArray(array: ByteArray, subArray: ByteArray): Int {
      return Collections.indexOfSubList(Arrays.asList(*ArrayUtils.toObject(array)), Arrays.asList(*ArrayUtils.toObject(subArray)))
    }
  }
}

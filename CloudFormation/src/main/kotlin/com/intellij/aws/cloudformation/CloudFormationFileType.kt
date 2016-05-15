package com.intellij.aws.cloudformation

import com.google.common.base.Charsets
import com.intellij.icons.AllIcons
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.FileSystemInterface
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile
import org.apache.commons.lang.ArrayUtils
import java.io.IOException
import java.util.Arrays
import java.util.Collections
import javax.swing.Icon

class CloudFormationFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {
  private val fileTypeRecursionGuard = RecursionManager.createGuard(javaClass.simpleName)

  override fun getName(): String = "AWSCloudFormation"
  override fun getDescription(): String = "AWS CloudFormation templates"
  override fun getDefaultExtension(): String = ""
  override fun getIcon(): Icon? = AllIcons.FileTypes.Json

  override fun isMyFileType(file: VirtualFile): Boolean {
    val extension = file.extension ?: return false

    return fileTypeRecursionGuard.doPreventingRecursion(javaClass.simpleName, false, {
      if (JsonFileType.DEFAULT_EXTENSION.equals(extension, ignoreCase = true) ||
          CloudFormationFileType.EXTENSION.equals(extension, ignoreCase = true) ||
          FileTypeManager.getInstance().getFileTypeByFile(file) === JsonFileType.INSTANCE) {
        return@doPreventingRecursion detectFromContent(file)
      }

      return@doPreventingRecursion false
    }) ?: false
  }

  private fun findArray(array: ByteArray, subArray: ByteArray): Int {
    return Collections.indexOfSubList(Arrays.asList(*ArrayUtils.toObject(array)), Arrays.asList(*ArrayUtils.toObject(subArray)))
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
      virtualFileSystem.getInputStream(file).use {
        val bytes = ByteArray(1024)

        val n = it.read(bytes, 0, bytes.size)
        return n > 0 && findArray(bytes, BYTES_TO_DETECT_CFN_FILE) >= 0
      }
    } catch (ignored: IOException) {
      return false
    }
  }

  companion object {
    val INSTANCE = CloudFormationFileType()

    private val EXTENSION = "template"
    private val BYTES_TO_DETECT_CFN_FILE = CloudFormationSections.FormatVersion.toByteArray(Charsets.US_ASCII)
  }
}

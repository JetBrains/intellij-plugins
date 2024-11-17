package org.jetbrains.qodana

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager

fun getFileTypeByFilename(filename: String): FileType {
  return FileTypeManager.getInstance().getFileTypeByFileName(filename)
}
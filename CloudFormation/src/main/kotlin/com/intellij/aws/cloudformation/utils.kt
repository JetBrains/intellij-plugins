package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.FileSystemInterface
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile
import org.apache.commons.lang.ArrayUtils
import java.io.IOException
import java.util.Arrays
import java.util.Collections

fun findSubArray(array: ByteArray, subArray: ByteArray): Int {
  return Collections.indexOfSubList(Arrays.asList(*ArrayUtils.toObject(array)), Arrays.asList(*ArrayUtils.toObject(subArray)))
}

inline fun <reified T> lookupSection(sections: List<CfnNode>): T? = sections.singleOrNull { it is T } as T?

fun detectFileTypeFromContent(file: VirtualFile, signatureBytes: ByteArray): Boolean {
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
      return n > 0 && findSubArray(bytes, signatureBytes) >= 0
    }
  } catch (ignored: IOException) {
    return false
  }
}

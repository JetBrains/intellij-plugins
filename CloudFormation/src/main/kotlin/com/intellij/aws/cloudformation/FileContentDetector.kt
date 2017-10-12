package com.intellij.aws.cloudformation

import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.FileSystemInterface
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile
import java.io.IOException

class FileContentDetector(moniker: String, private val signature: ByteArray) {
  private val cacheKey = Key.create<DetectedSignatureCache>("CloudFormationDetectedSignature:$moniker")

  private data class DetectedSignatureCache(val stamp: Long, val signatureDetected: Boolean)

  fun detectSignature(file: VirtualFile): Boolean {
    if (file is StubVirtualFile) {
      // Helps New -> File get correct file type
      return true
    }

    val timeStamp = try {
      file.modificationStamp
    } catch (e: UnsupportedOperationException) {
      file.timeStamp
    }

    val cached = file.getUserData(cacheKey)
    if (cached != null && cached.stamp == timeStamp) {
      return cached.signatureDetected
    }

    val value = detectSignatureFromContent(file)
    file.putUserData(cacheKey, DetectedSignatureCache(timeStamp, value))
    return value
  }

  private fun detectSignatureFromContent(file: VirtualFile): Boolean {
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
        val bytes = ByteArray(4096)

        val n = it.read(bytes, 0, bytes.size)
        return n > 0 && findSubArray(bytes, signature) >= 0
      }
    } catch (ignored: IOException) {
      return false
    }
  }
}
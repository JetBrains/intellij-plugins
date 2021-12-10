package com.intellij.protobuf.ide.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.URLUtil
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

internal object PbBundledResourcesUtil {
  fun extractResources(bundledResourceUrl: URL?, targetDirectory: Path) {
    if (bundledResourceUrl == null) {
      thisLogger().warn("Unable to extract bundled proto files because resource URL is null")
      return
    }
    if (ApplicationManager.getApplication().isUnitTestMode)
      extractResourcesFromDirectoryRecursively(bundledResourceUrl, targetDirectory)
    else
      extractResourcesFromJarRecursively(bundledResourceUrl, targetDirectory)
  }

  private fun extractResourcesFromDirectoryRecursively(bundledResourceUrl: URL, targetDirectory: Path) {
    Files.createDirectories(targetDirectory)
    FileUtil.copyDir(URLUtil.urlToFile(bundledResourceUrl), targetDirectory.toFile())
  }

  private fun extractResourcesFromJarRecursively(bundledJarResourceUrl: URL?, targetDirectory: Path) {
    val urlStringRepresentation = bundledJarResourceUrl?.toExternalForm() ?: run {
      thisLogger().warn("Unable to construct external URL form for $bundledJarResourceUrl. Abort resources unpacking")
      return
    }

    val splitJarPath = splitJarPath(urlStringRepresentation)
    val mayBeEscapedFile = URL(splitJarPath.first).file
    val file = URLUtil.unescapePercentSequences(mayBeEscapedFile)
    val jarFile = JarFile(file)
    val prefix = splitJarPath.second

    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      if (entry.name.startsWith(prefix)) {
        val filename = StringUtil.trimStart(entry.name, prefix)
        val targetFileOrDirectory = File(targetDirectory.toFile(), filename)

        if (!entry.isDirectory) {
          if (!ensureDirectoryExists(targetFileOrDirectory.parentFile)) {
            thisLogger().error("Cannot create directory: " + targetFileOrDirectory.parentFile)
          }
          jarFile.getInputStream(entry).use {
            copyStream(it, FileOutputStream(targetFileOrDirectory))
          }
        }
      }
    }
  }

  private fun splitJarPath(path: String): Pair<String, String> {
    val lastIndexOf = path.lastIndexOf(".jar!/")
    if (lastIndexOf == -1) throw IOException("Invalid Jar path format")
    val splitIdx = lastIndexOf + ".jar".length
    val filePath = path.substring(0, splitIdx)
    val pathInsideJar = path.substring(splitIdx + "!/".length, path.length)
    return Pair(filePath, pathInsideJar)
  }

  private fun ensureDirectoryExists(directory: File): Boolean = directory.exists() || directory.mkdirs()

  private fun copyStream(inputStream: InputStream, os: OutputStream) {
    val buf = ByteArray(1024)
    var len = inputStream.read(buf)
    while (len > 0) {
      os.write(buf, 0, len)
      len = inputStream.read(buf)
    }
    inputStream.close()
    os.close()
  }
}
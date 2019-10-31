/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.project

import org.apache.commons.lang.StringUtils
import java.io.*
import java.net.JarURLConnection
import java.net.URL

object FileUtils {

  fun copyFile(toCopy: File, destFile: File): Boolean {
    try {
      return FileUtils.copyStream(FileInputStream(toCopy),
          FileOutputStream(destFile))
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }

    return false
  }

  private fun copyFilesRecursively(toCopy: File, destDir: File): Boolean {
    assert(destDir.isDirectory)

    if (!toCopy.isDirectory) {
      return FileUtils.copyFile(toCopy, File(destDir, toCopy.name))
    } else {
      val newDestDir = File(destDir, toCopy.name)
      if (!newDestDir.exists() && !newDestDir.mkdir()) {
        return false
      }
      for (child in toCopy.listFiles()) {
        if (!FileUtils.copyFilesRecursively(child, newDestDir)) {
          return false
        }
      }
    }
    return true
  }

  @Throws(IOException::class)
  fun copyJarResourcesRecursively(destDir: File,
                                  jarConnection: JarURLConnection): Boolean {

    val jarFile = jarConnection.jarFile

    val entries = jarFile.entries()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      if (entry.name.startsWith(jarConnection.entryName)) {
        val filename = StringUtils.removeStart(entry.name, jarConnection.entryName)

        val f = File(destDir, filename)
        if (!entry.isDirectory) {
          val entryInputStream = jarFile.getInputStream(entry)
          if (!FileUtils.copyStream(entryInputStream, f)) {
            return false
          }
          entryInputStream.close()
        } else {
          if (!FileUtils.ensureDirectoryExists(f)) {
            throw IOException("Could not create directory: " + f.absolutePath)
          }
        }
      }
    }
    return true
  }

  fun copyResourcesRecursively(originUrl: URL, destination: File): Boolean {
    try {
      val urlConnection = originUrl.openConnection()
      return if (urlConnection is JarURLConnection)
        FileUtils.copyJarResourcesRecursively(destination, urlConnection)
      else
        FileUtils.copyFilesRecursively(File(originUrl.path), destination)
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return false
  }

  private fun copyStream(inputStream: InputStream, f: File): Boolean {
    try {
      return FileUtils.copyStream(inputStream, FileOutputStream(f))
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }
    return false
  }

  private fun copyStream(inputStream: InputStream, os: OutputStream): Boolean {
    try {
      val buf = ByteArray(1024)
      var len = inputStream.read(buf)
      while (len > 0) {
        os.write(buf, 0, len)
        len = inputStream.read(buf)
      }
      inputStream.close()
      os.close()
      return true
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return false
  }

  fun ensureDirectoryExists(f: File): Boolean = f.exists() || f.mkdir()
}
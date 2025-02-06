package com.jetbrains.plugins.meteor.runner

import com.google.gson.stream.JsonReader
import com.intellij.javascript.debugger.DebuggableFileFinder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.intellij.util.ThreeState
import com.intellij.util.Url
import com.intellij.util.Urls
import org.jetbrains.debugger.sourcemap.SourceResolver
import java.io.File

private const val METEOR_LOCAL_PACKAGE_SOURCE_PREFIX = "meteor://\uD83D\uDCBBapp/"
private const val PACKAGES_PREFIX = "packages/"
private const val DOT_METEOR = ".meteor"
private val LOG = logger<MeteorFileFinder>()

/**
 * @param workDir System-independent path
 */
class MeteorFileFinder(workDir: String) : DebuggableFileFinder {
  private val virtualDir: VirtualFile? = LocalFileSystem.getInstance().findFileByPath(workDir)
  private val meteorDir: VirtualFile? = virtualDir?.findChild(DOT_METEOR)
  private val isopacksDir: File = File(workDir, DOT_METEOR + File.separatorChar + "local" + File.separatorChar + "isopacks")

  private var localPackagesExists = ThreeState.UNSURE

  init {
    LOG.assertTrue(virtualDir != null)
    LOG.assertTrue(meteorDir != null)
  }

  override fun guessFile(url: Url, project: Project): VirtualFile? {
    val file = findFileForMeteorSpecificUrl(url)
    //try to find file in work dir (should be part of path)
    return if (file == null && virtualDir != null) findFileInWorkingDirectory(url.path) else file
  }

  private fun findFileInWorkingDirectory(path: String): VirtualFile? {
    var i = path.indexOf('/')
    while (i != -1) {
      val subPath = path.substring(i + 1, path.length)
      assert(virtualDir != null)
      val file = virtualDir!!.findFileByRelativePath(subPath)
      if (file != null) {
        return file
      }
      i = path.indexOf('/', i + 2)
    }

    return null
  }

  override fun isOnlySourceMappedBreakpoints(file: VirtualFile): Boolean {
    val result = super.isOnlySourceMappedBreakpoints(file)
    return if (!result && meteorDir != null) {
      // file outside of .meteor is always compiled to .meteor/..., so, we should try to not set breakpoints in it directly
      !VfsUtilCore.isAncestor(meteorDir, file, true)
    }
    else result
  }

  private fun findFileForMeteorSpecificUrl(url: Url): VirtualFile? {
    try {
      val scriptPath = url.path
      val urlString = url.toString()
      val meteorDirSlashedName = "/$DOT_METEOR/"
      val index = scriptPath.lastIndexOf(meteorDirSlashedName)
      if (index != -1 && scriptPath.startsWith("local/build/programs/server/app/", index + meteorDirSlashedName.length)) {
        // it is app script
        val sourceRoot = scriptPath.substring(0, index + 1)
        LOG.assertTrue(urlString.startsWith(METEOR_LOCAL_PACKAGE_SOURCE_PREFIX))
        return LocalFileSystem.getInstance().findFileByPath("$sourceRoot${urlString.substring(METEOR_LOCAL_PACKAGE_SOURCE_PREFIX.length)}")
      }

      return findFileForLocalPackage(url)
    }
    catch (e: Exception) {
      LOG.warn(e)
    }
    return null
  }

  // https://youtrack.jetbrains.com/issue/WEB-13490
  // /Users/develar/WebStorm-cant-debug-Meteor-packages/.meteor/local/build/programs/server/packages/pdftk:pdftk/pdftk-wrapper.js -> /Users/develar/WebStorm-cant-debug-Meteor-packages/packages/pdftk:pdftk/pdftk-wrapper.js
  private fun findFileForLocalPackage(url: Url): VirtualFile? {
    val urlString = url.toString()
    var packageName: String? = null
    var packageFileName: String? = null
    var prefixLength: Int = -1

    if (urlString.startsWith(METEOR_LOCAL_PACKAGE_SOURCE_PREFIX)) {
      val fileName = PathUtil.getFileName(url.path)
      val dotIndex = fileName.indexOf('.')
      if (dotIndex > 0) {
        val firstUnderscoreIndex = fileName.indexOf('_', 0)
        if (firstUnderscoreIndex > 0 && firstUnderscoreIndex < dotIndex) {
          packageFileName = fileName.substring(0, dotIndex)
          // develar_simple -> develar:simple
          packageName = fileName.substring(0, firstUnderscoreIndex) + ':' + fileName.substring(firstUnderscoreIndex + 1, dotIndex)
        }
        else {
          // unprefixed package
          packageFileName = fileName.substring(0, dotIndex)
          packageName = packageFileName
        }

        // -1 to add /
        prefixLength = METEOR_LOCAL_PACKAGE_SOURCE_PREFIX.length - 1
        if (urlString.startsWith(PACKAGES_PREFIX, METEOR_LOCAL_PACKAGE_SOURCE_PREFIX.length)) {
          prefixLength += PACKAGES_PREFIX.length
          // packages/PACKAGE_NAME/
          if (urlString.getOrNull(prefixLength + 1 + packageName.length) == '/' &&
              (urlString.startsWith(packageFileName, prefixLength + 1) || urlString.startsWith(packageName, prefixLength + 1))
            ) {
            prefixLength += packageName.length + 1
          }
        }
      }
    }
    else if (!SourceResolver.isAbsolute(urlString)) {
      val i = urlString.indexOf('/')
      if (i > 0) {
        packageName = urlString.substring(0, i)
      }
    }

    if (packageName == null) {
      return null
    }
    val isopacksDir = isopacksDir
    if (localPackagesExists == ThreeState.UNSURE) {
      localPackagesExists = ThreeState.fromBoolean(isopacksDir.exists())
    }

    if (localPackagesExists == ThreeState.NO) {
      return null
    }

    //noinspection SpellCheckingInspection
    val buildInfo = File(isopacksDir, "${packageFileName ?: packageName.replace(':', '_')}${File.separatorChar}isopack-buildinfo.json")
    if (!buildInfo.exists()) {
      return null
    }

    var sourceRoot = readSourceRoot(buildInfo, packageName) ?: return null
    // correct path /C/Users/develar/Desktop/WEB-16337/packages/simple -> C:/Users/develar/Desktop/WEB-16337/packages/simple
    if (SystemInfo.isWindows && sourceRoot.length > 4 && sourceRoot[0] == '/' && sourceRoot[2] == '/') {
      sourceRoot = "${sourceRoot[1]}:${sourceRoot.substring(2)}"
    }

    if (prefixLength == -1) {
      prefixLength = packageName.length
    }

    return LocalFileSystem.getInstance().findFileByPath("$sourceRoot${urlString.substring(prefixLength)}")
  }

  private fun readSourceRoot(buildInfo: File, packageName: String?): String? {
    var sourceRoot: String? = null
    JsonReader(buildInfo.inputStream().reader()).use { reader ->
      if (reader.readUntilName("pluginProviderPackageMap") && reader.readUntilName(packageName!!)) {
        reader.beginObject()
        var kind: String? = null
        while (reader.hasNext()) {
          when (reader.nextName()) {
            "kind" -> kind = reader.nextString()
            "sourceRoot" -> sourceRoot = reader.nextString()
            else -> reader.skipValue()
          }
        }

        if ("local" != kind) {
          return null
        }
      }
    }
    return sourceRoot
  }

  override fun getRemoteUrls(file: VirtualFile): List<Url> {
    return listOf(Urls.newLocalFileUrl(file.path))
  }
}

private fun JsonReader.readUntilName(wantedName: String): Boolean {
  beginObject()
  while (hasNext()) {
    if (nextName() == wantedName) {
      return true
    }
    else {
      skipValue()
    }
  }
  return false
}
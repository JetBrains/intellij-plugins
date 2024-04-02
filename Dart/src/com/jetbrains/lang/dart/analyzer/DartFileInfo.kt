package com.jetbrains.lang.dart.analyzer

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.OSAgnosticPathUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.net.URI
import java.net.URISyntaxException

sealed class DartFileInfo {
  abstract fun findFile(): VirtualFile?
}

data class DartLocalFileInfo(val filePath: String) : DartFileInfo() {
  override fun findFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByPath(filePath)
}

data class DartNotLocalFileInfo(private val project: Project, val fileUri: String) : DartFileInfo() {
  override fun findFile(): VirtualFile? =
    DartAnalysisServerService.getInstance(project).getNotLocalVirtualFile(fileUri)
}


fun getDartFileInfo(project: Project, filePathOrUri: String): DartFileInfo = when {
  !filePathOrUri.contains("://") -> DartLocalFileInfo(FileUtil.toSystemIndependentName(filePathOrUri))
  !filePathOrUri.startsWith("file://") -> DartNotLocalFileInfo(project, filePathOrUri)
  else -> try {
    var path = URI(filePathOrUri).path
    if (SystemInfo.isWindows && path.length >= 3 && path[0] == '/' && OSAgnosticPathUtil.startsWithWindowsDrive(path.substring(1))) {
      path = path.substring(1)
    }
    DartLocalFileInfo(path)
  }
  catch (e: URISyntaxException) {
    logger<DartFileInfo>().warn("Malformed URI: $filePathOrUri")
    DartNotLocalFileInfo(project, filePathOrUri)
  }
}

@JvmField
val DART_NOT_LOCAL_FILE_URI_KEY: Key<String> = Key.create("DART_NOT_LOCAL_FILE_URI")

fun getDartFileInfo(project: Project, virtualFile: VirtualFile): DartFileInfo =
  virtualFile.getUserData(DART_NOT_LOCAL_FILE_URI_KEY)
    ?.let { DartNotLocalFileInfo(project, it) }
  ?: DartLocalFileInfo(virtualFile.path)

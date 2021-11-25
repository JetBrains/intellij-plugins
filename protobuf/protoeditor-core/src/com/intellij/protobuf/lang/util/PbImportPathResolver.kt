package com.intellij.protobuf.lang.util

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.lang.PbFileType
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import java.nio.file.Paths

internal object PbImportPathResolver {
  private const val MAX_LOCATION_LENGTH = 100

  fun findSuitableImportPaths(importStatement: String, editedFile: VirtualFile, project: Project): List<ImportPathData> {
    val relativeProtoPath = FileUtil.toSystemIndependentName(importStatement)
    return DumbService.getInstance(project).runReadActionInSmartMode(Computable {
      FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        .asSequence()
        .map { FileUtil.toSystemIndependentName(it.path) }
        .filter { it.endsWith(relativeProtoPath) }
        .map { it.removeSuffix(relativeProtoPath).removeSuffix("/") }
        .filter { it.isNotBlank() }
        .distinct()
        .mapNotNull { VfsUtil.findFile(Paths.get(it), false) }
        .map { ImportPathData(it, editedFile, shortenPath(it, project), importStatement) }
        .toList()
    })
  }

  private fun shortenPath(virtualFile: VirtualFile, project: Project): String {
    val sourceRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(virtualFile)
    val relativePath = sourceRoot?.let {
      VfsUtilCore.getRelativePath(virtualFile, it)
    } ?: virtualFile.path

    val systemDependentPath = FileUtil.toSystemDependentName(relativePath)

    return if (systemDependentPath.length <= MAX_LOCATION_LENGTH)
      systemDependentPath
    else
      "...${systemDependentPath.substring(systemDependentPath.length - MAX_LOCATION_LENGTH)}"
  }
}

internal data class ImportPathData(
  val effectiveImportVirtualFile: VirtualFile,
  val originalPbVirtualFile: VirtualFile,
  @NlsSafe val presentablePath: String,
  @NlsSafe val originalImportStatement: String
)
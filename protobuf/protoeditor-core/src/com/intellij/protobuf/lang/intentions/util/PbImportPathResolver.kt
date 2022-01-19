package com.intellij.protobuf.lang.intentions.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.intentions.util.ImportPathData.Companion.shortenPath
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

internal object PbImportPathResolver {
  fun findSuitableImportPaths(importStatement: String, editedFile: VirtualFile, project: Project): Sequence<ImportPathData> {
    val relativeImportPath = FileUtil.toSystemIndependentName(importStatement)
    return FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      .asSequence()
      .mapNotNull { findEffectiveImportPath(relativeImportPath, it) }
      .map { ImportPathData(editedFile, it, importStatement, it.url, shortenPath(it, project)) }
  }

  private fun findEffectiveImportPath(relativeImportPath: String, protoFileCandidate: VirtualFile): VirtualFile? {
    val absoluteImportPath = FileUtil.toSystemIndependentName(protoFileCandidate.url).takeIf { it.endsWith(relativeImportPath) }
                             ?: return null
    val effectiveImportPathUrl = absoluteImportPath.removeSuffix(relativeImportPath).removeSuffix("/")

    // one can not simply call VfsUtil.findFile with effectiveUrl arg since unit test TempFileSystem will not handle such request correctly
    return generateSequence(protoFileCandidate, VirtualFile::getParent)
      .firstOrNull { FileUtil.toSystemIndependentName(it.url) == effectiveImportPathUrl }
  }
}

internal class ImportPathData(
  val originalPbVirtualFile: VirtualFile,
  val importedPbVirtualFile: VirtualFile,
  @NlsSafe val originalImportStatement: String,
  @NlsSafe val effectiveImportPathUrl: String,
  @NlsSafe val presentablePath: String
) {
  companion object {
    private const val MAX_LOCATION_LENGTH = 60

    fun shortenPath(virtualFile: VirtualFile, project: Project): String {
      val sourceRoot = ProjectFileIndex.getInstance(project).getSourceRootForFile(virtualFile)
      val relativePath = sourceRoot?.let {
        VfsUtilCore.getRelativePath(virtualFile, it)
      } ?: virtualFile.path

      val systemDependentPath = FileUtil.toSystemDependentName(relativePath)
      val computedSuffixLength = MAX_LOCATION_LENGTH - StringUtil.THREE_DOTS.length
      return StringUtil.shortenTextWithEllipsis(systemDependentPath, MAX_LOCATION_LENGTH, computedSuffixLength, StringUtil.THREE_DOTS)
    }
  }
}
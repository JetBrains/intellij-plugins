package com.intellij.dts.lang.resolve.files

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * Resolves references to the source file from an overlay file.
 */
object DtsOverlayFile : FileInclude {
  override val offset: Int? = null

  override fun resolve(anchor: PsiFile): PsiFile? {
    val context = anchor.originalFile.virtualFile
    if (context == null || !context.isValid) return null

    val name = "${context.nameWithoutExtension}.dts"
    val board = DtsZephyrProvider.of(anchor.project).board ?: return null

    val candidates = mutableListOf<VirtualFile>()
    VfsUtilCore.processFilesRecursively(board.file) { virtualFile ->
      if (virtualFile.name == name) {
        candidates.add(virtualFile)
      }

      true
    }

    // TODO: filter for best matching candidate

    return candidates.asSequence().map { virtualFile -> anchor.manager.findFile(virtualFile) }.firstOrNull()
  }
}
package com.intellij.dts.lang.resolve.files

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.util.DtsZephyrUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * Resolves references to the source file from an overlay file.
 */
object DtsOverlayFile : FileInclude {
    override val offset: Int = 0

    override fun resolve(anchor: PsiFile): PsiFile? {
        val context = anchor.originalFile.virtualFile
        if (context == null || !context.isValid) return null

        val name = "${context.nameWithoutExtension}.dts"
        val boards = DtsZephyrUtil.getBoardDir(anchor.project) ?: return null

        val candidates = mutableListOf<VirtualFile>()
        VfsUtilCore.processFilesRecursively(boards) {
            if (it.name == name) {
                candidates.add(it)
            }

            true
        }

        // TODO: filter for best matching candidate

        return candidates.asSequence().map { anchor.manager.findFile(it) }.firstOrNull()
    }
}
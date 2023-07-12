package com.intellij.dts.lang.resolve.files

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.openapi.project.modules
import com.intellij.openapi.project.rootManager
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

        val candidates = mutableListOf<VirtualFile>()
        for (module in anchor.project.modules) {
            module.rootManager.fileIndex.iterateContent(candidates::add) {
                it.name == name
            }
        }

        // TODO: actually filter the candidates to remove non zephyr files

        return candidates.asSequence().map { anchor.manager.findFile(it) }.firstOrNull()
    }
}
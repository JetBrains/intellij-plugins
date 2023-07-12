package com.intellij.dts.lang.resolve.files

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.psi.PsiFile

/**
 * Resolve references to relative files.
 */
class DtsIncludeFile(
    private val path: String,
    override val offset: Int,
) : FileInclude {
    override fun resolve(anchor: PsiFile): PsiFile? {
        val context = anchor.originalFile.virtualFile
        if (context == null || !context.isValid) return null

        val file = context.parent?.findFileByRelativePath(path) ?: return null

        return anchor.manager.findFile(file)
    }
}
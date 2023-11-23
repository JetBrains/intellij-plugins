package com.intellij.dts.lang.resolve.files

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.psi.PsiFile

/**
 * Resolve references to relative files and searches in zephyr include dirs.
 */
class DtsIncludeFile(val path: String, override val offset: Int) : FileInclude {
  private fun findRelativeFile(anchor: PsiFile): PsiFile? {
    val context = anchor.originalFile.virtualFile
    if (context?.isValid != true) return null

    val file = context.parent?.findFileByRelativePath(path) ?: return null
    return anchor.manager.findFile(file)
  }

  private fun findInIncludeDirs(anchor: PsiFile): PsiFile? {
    val manager = anchor.manager
    val includes = DtsZephyrProvider.of(anchor.project).getIncludeDirs()

    for (include in includes) {
      val file = include.findFileByRelativePath(path) ?: continue
      return manager.findFile(file) ?: continue
    }

    return null
  }

  override fun resolve(anchor: PsiFile): PsiFile? = findRelativeFile(anchor) ?: findInIncludeDirs(anchor)
}
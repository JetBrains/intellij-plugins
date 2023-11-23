package com.intellij.dts.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

interface FileInclude {
  /**
   * The offset where the file is included. Null if there is no particular
   * offset for the include and the content of the included fiel can be
   * accessed from anywhere in the file.
   */
  val offset: Int?

  fun resolve(anchor: PsiFile): PsiFile?
}

/**
 * Helper method to check whether the content of the included file is accessible
 * from a specific offset.
 *
 * Returns true if the offset is null.
 */
fun FileInclude.before(maxOffset: Int?): Boolean {
  if (maxOffset == null) return true

  val offset = this.offset
  return offset == null || offset < maxOffset
}

/**
 * Represents a declaration which imports another file. Used for dts and c
 * preprocessor includes.
 */
interface PsiFileInclude : PsiElement {
  val fileInclude: FileInclude?

  val fileIncludeRange: TextRange?
}
// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.lang.javascript.psi.JSBlockStatement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil

internal fun extendRange(file: PsiFile, rangeToReformat: TextRange): TextRange {
  val start = file.findElementAt(rangeToReformat.startOffset) ?: return rangeToReformat
  val end = file.findElementAt(rangeToReformat.endOffset - 1) ?: return rangeToReformat

  if (rangeToReformat.startOffset != 0 || rangeToReformat.endOffset != file.textRange.endOffset) {
    val commonParent = PsiTreeUtil.findCommonParent(start, end)
    if (commonParent is JSBlockStatement) {
      val parentRange = commonParent.textRange
      // If a format range ends exactly on a boundary between two nodes, a trailing node will be included in the formatting range.
      // So, for this selection `{  <selection>  abc</selection>}`
      // we get this formatting range `{<selection>    abc}</selection>`.
      // In such cases, prettier can go crazy and insert a comma somewhere in front of the block in the wrong place.
      if (!rangeToReformat.contains(parentRange) &&
          (rangeToReformat.startOffset == parentRange.startOffset || rangeToReformat.endOffset == parentRange.endOffset)) {
        return parentRange
      }
    }
  }

  if (end is PsiWhiteSpace || end is PsiComment) {
    // https://github.com/prettier/prettier/issues/15445
    var maybeComment = end
    var prev = PsiTreeUtil.prevLeaf(end, true)
    while (prev is PsiWhiteSpace || prev is PsiComment) {
      maybeComment = prev.takeIf { it is PsiComment } ?: maybeComment
      prev = PsiTreeUtil.prevLeaf(prev, true)
    }
    if (maybeComment is PsiComment) {
      return TextRange.create(rangeToReformat.startOffset, maybeComment.textRange.startOffset)
    }
  }

  return rangeToReformat
}
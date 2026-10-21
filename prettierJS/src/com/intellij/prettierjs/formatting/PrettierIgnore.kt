// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parents

/** A comment whose body is `prettier-ignore`, in the comment syntaxes of the languages that Prettier formats by range. */
private val PRETTIER_IGNORE_COMMENT = Regex("""(?://|/\*|#)\s*prettier-ignore\s*(?:\*/)?""")

/**
 * Returns `true` when [range] starts inside an element that a `prettier-ignore` comment precedes.
 *
 * Prettier formats the smallest statements that cover a range.
 * It does not see a `prettier-ignore` comment before such a statement, so it formats the element that the comment protects.
 * A format of the whole file keeps the element as is. The caller must not format such a range.
 */
internal fun isInsidePrettierIgnoredElement(file: PsiFile, range: TextRange): Boolean {
  val start = findFirstElement(file, range) ?: return false
  return start.parents(withSelf = true)
    .takeWhile { it !is PsiFile }
    .any { hasLeadingPrettierIgnoreComment(it) }
}

/**
 * The first element in [range] that is not whitespace.
 * When [range] holds only whitespace, the result is the whitespace, because Prettier then formats the element around it.
 */
private fun findFirstElement(file: PsiFile, range: TextRange): PsiElement? {
  val start = file.findElementAt(range.startOffset) ?: return null
  if (start !is PsiWhiteSpace) return start
  var leaf = PsiTreeUtil.nextLeaf(start)
  while (leaf is PsiWhiteSpace) leaf = PsiTreeUtil.nextLeaf(leaf)
  return leaf?.takeIf { it.textRange.startOffset < range.endOffset } ?: start
}

/** Prettier attaches a comment to the next element when only whitespace and other comments separate them. */
private fun hasLeadingPrettierIgnoreComment(element: PsiElement): Boolean {
  var sibling = element.prevSibling
  while (sibling != null) {
    when (sibling) {
      is PsiComment -> if (isPrettierIgnoreComment(sibling) && !isTrailingComment(sibling)) return true
      !is PsiWhiteSpace -> if (sibling.textLength > 0) return false
    }
    sibling = sibling.prevSibling
  }
  return false
}

private fun isPrettierIgnoreComment(comment: PsiComment): Boolean = PRETTIER_IGNORE_COMMENT.matches(comment.text)

/**
 * Prettier attaches a comment to the element that ends on the same line before the comment.
 * A token such as `{` or `,` is not an element, so a comment after a token still leads the next element.
 */
private fun isTrailingComment(comment: PsiComment): Boolean {
  var sibling = comment.prevSibling
  while (sibling != null) {
    when (sibling) {
      is PsiWhiteSpace, is PsiComment -> if (sibling.textContains('\n')) return false
      else -> if (sibling.firstChild != null) return true
    }
    sibling = sibling.prevSibling
  }
  return false
}

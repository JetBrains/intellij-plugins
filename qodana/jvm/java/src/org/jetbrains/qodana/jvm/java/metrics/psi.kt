package org.jetbrains.qodana.jvm.java.metrics

import com.intellij.openapi.editor.Document
import com.intellij.psi.*
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset

data class LineRange(val startLine: Int, val endLine: Int) {
  companion object {
    val EMPTY = LineRange(-1, -1)
  }
}

fun PsiElement?.getStartLine(document: Document): Int {
  if (this == null) return -1

  val startOffset: Int = this.startOffset
  val startLine: Int = document.getLineNumber(startOffset)
  return startLine
}

fun PsiElement?.getEndLine(document: Document): Int {
  if (this == null) return -1

  val endOffset: Int = this.endOffset
  val endLine: Int = document.getLineNumber(endOffset)
  return endLine
}

fun PsiElement?.getLineRange(document: Document): LineRange {
  if (this == null) return LineRange.EMPTY
  return LineRange(startLine = this.getStartLine(document), endLine = this.getEndLine(document))
}

/**
 * Function that checks whether current [PsiElement] has sibling on a [lineNumber] line.
 * If [ignoreWhitespace] is `true` siblings of type [PsiWhiteSpace] will be ignored.
 *
 * @param document document that contains the element.
 * @param lineNumber the line number for which the checking is performed.
 * @param forward when `true`, siblings that come after the current element will be checked.
 * @param ignoreWhitespace indicates that whitespace siblings should be ignored.
 * @return `true` if it has sibling on the same line, `false` otherwise.
 */
fun PsiElement?.hasSiblingOnTheLine(document: Document, lineNumber: Int, forward: Boolean = true, ignoreWhitespace: Boolean = false): Boolean {
  if (this == null) return false

  val sibling: PsiElement? = this.siblings(forward = forward, withSelf = false)
    .dropWhile { e ->
      (ignoreWhitespace && e is PsiWhiteSpace) ||
      (e is PsiImportList && e.children.isEmpty()) ||
      (e is PsiPackageStatement && e.children.isEmpty())
    }.firstOrNull()

  val hasSibling: Boolean = sibling != null
  if (hasSibling) {
    val offset: Int = if (forward) sibling!!.startOffset else sibling!!.endOffset
    val siblingLine: Int = document.getLineNumber(offset)
    return siblingLine == lineNumber
  }

  return false
}

fun PsiElement?.getNumberOfLinesWhereOnlyElementOnALine(document: Document, ignoreWhitespace: Boolean): Int {
  if (this == null) return -1

  val (elementStartLine, elementEndLine) = this.getLineRange(document)
  var elementLines: Int = elementEndLine - elementStartLine + 1

  val prevSiblingOnTheSameLine: Boolean = this.hasSiblingOnTheLine(document, lineNumber = elementStartLine, forward = false, ignoreWhitespace)
  if (prevSiblingOnTheSameLine) {
    elementLines--
  }

  val nextSiblingOnTheSameLine: Boolean = this.hasSiblingOnTheLine(document, lineNumber = elementEndLine, forward = true, ignoreWhitespace)
  if (nextSiblingOnTheSameLine) {
    elementLines--
  }

  if (elementLines < 0) elementLines = 0

  return elementLines
}

fun PsiElement?.hasFirstAndLastChildOnTheSameLine(document: Document): Boolean {
  if (this == null) return false

  val firstChild: PsiElement? = this.firstChild
  val lastChild: PsiElement? = this.lastChild

  if (firstChild != null && lastChild != null) {
    return firstChild.getStartLine(document) == lastChild.getEndLine(document)
  }

  return false
}
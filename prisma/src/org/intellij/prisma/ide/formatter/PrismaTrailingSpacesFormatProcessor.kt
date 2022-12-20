package org.intellij.prisma.ide.formatter

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.containers.addIfNotNull
import com.intellij.util.text.CharArrayUtil
import org.intellij.prisma.lang.psi.PrismaElementTypes.ENUM_VALUE_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.FIELD_DECLARATION
import org.intellij.prisma.lang.psi.PrismaFile
import org.intellij.prisma.lang.psi.skipWhitespacesBackwardWithoutNewLines

class PrismaTrailingSpacesFormatProcessor : PostFormatProcessor {
  private fun isApplicable(file: PsiFile): Boolean {
    return file is PrismaFile
  }

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    processText(source.containingFile, source.textRange, settings)
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (!isApplicable(source)) {
      return rangeToReformat
    }

    val manager = PsiDocumentManager.getInstance(source.project)
    val document = manager.getDocument(source) ?: return rangeToReformat
    val ranges = buildList {
      addAll(findTrailingWhitespaceRanges(source, rangeToReformat))
      addIfNotNull(findTrailingEofLineRange(document, rangeToReformat))
    }.sortedByDescending { it.first.endOffset }

    if (ranges.isEmpty()) {
      return rangeToReformat
    }

    manager.doPostponedOperationsAndUnblockDocument(document)
    var delta = 0
    for ((range, replacement) in ranges) {
      document.replaceString(range.startOffset, range.endOffset, replacement)
      delta += replacement.length - range.length
    }
    manager.commitDocument(document)
    return rangeToReformat.grown(delta)
  }

  private fun findTrailingWhitespaceRanges(file: PsiFile, rangeToReformat: TextRange): List<Pair<TextRange, String>> {
    val ranges = mutableListOf<Pair<TextRange, String>>()
    val whiteSpaces = SyntaxTraverser.psiTraverser(file).onRange(rangeToReformat).filter(PsiWhiteSpace::class.java)
    for (ws in whiteSpaces) {
      val prev = ws.skipWhitespacesBackwardWithoutNewLines()
      val elementType = prev?.elementType
      if (elementType == FIELD_DECLARATION || elementType == ENUM_VALUE_DECLARATION) {
        val newLineIdx = ws.text.indexOf('\n')
        // We remove trailing spaces added because of the alignment by a field attribute.
        // Comments are also aligned by a field attribute, but they shouldn't, so
        // whitespaces that were added between a field type and a trailing comment are also removed
        //
        // model M {
        //   id Int<-----trailing spaces------ >
        //   longFieldNameWithAttribute String @id
        //   name Int<-----trailing spaces---->// comment
        // }
        if (newLineIdx >= 0) {
          ranges.add(TextRange.from(ws.startOffset, newLineIdx) to "")
        }
        else if (ws.nextSibling is PsiComment) {
          ranges.add(TextRange.from(ws.startOffset, ws.textLength) to " ")
        }
      }
    }
    return ranges
  }

  private fun findTrailingEofLineRange(document: Document, rangeToReformat: TextRange): Pair<TextRange, String>? {
    val chars = document.charsSequence
    val from = CharArrayUtil.shiftBackward(chars, chars.length - 1, " \r\n\t")
    val range = TextRange.create(from + 1, chars.length)
    return if (!rangeToReformat.intersects(range) || document.getText(range) == "\n") {
      null
    }
    else {
      range to "\n"
    }
  }
}
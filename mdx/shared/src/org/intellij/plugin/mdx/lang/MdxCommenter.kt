// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.plugin.mdx.lang

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.generation.CommenterDataHolder
import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.codeInsight.generation.SelfManagingCommenter
import com.intellij.codeInsight.generation.SelfManagingCommenterUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.text.CharArrayUtil
import org.intellij.plugin.mdx.js.MdxJSLanguage

internal class MdxCommenter(private val tagContext: MdxCommentContext = MdxCommentContext.MDX) : IndentedCommenter,
                                                                                                 SelfManagingCommenter<MdxCommenter.State> {
  override fun getLineCommentPrefix(): String? = null

  override fun getBlockCommentPrefix(): String = "{/*"

  override fun getBlockCommentSuffix(): String = "*/}"

  override fun getCommentedBlockCommentPrefix(): String? = null

  override fun getCommentedBlockCommentSuffix(): String? = null

  override fun forceIndentedLineComment(): Boolean = true

  override fun createLineCommentingState(startLine: Int, endLine: Int, document: Document, file: PsiFile): State {
    return createState(document, file, TextRange(document.getLineStartOffset(startLine), document.getLineEndOffset(endLine)))
      .copy(skipEmptyLines = startLine != endLine)
  }

  override fun createBlockCommentingState(selectionStart: Int, selectionEnd: Int, document: Document, file: PsiFile): State {
    return createState(document, file, TextRange(selectionStart, selectionEnd), block = true)
  }

  private fun createState(document: Document, file: PsiFile, range: TextRange, block: Boolean = false): State {
    PsiDocumentManager.getInstance(file.project).commitDocument(document)
    val existing = if (block) selectedBlockComment(document, range) ?: commentAtCaret(file, document, range) else null
    val context = existing?.first ?: mdxCommentContext(file, trimmedRange(document, range), tagContext)
    val language = if (context == MdxCommentContext.JAVASCRIPT) MdxJSLanguage.INSTANCE else MdxLanguage
    val settings = CodeStyle.getLanguageSettings(file, language)
    return State(context, settings.LINE_COMMENT_ADD_SPACE, settings.BLOCK_COMMENT_ADD_SPACE, existing?.second)
  }

  override fun getCommentPrefix(line: Int, document: Document, data: State): String = data.context.linePrefix ?: data.context.blockPrefix

  override fun isLineCommented(line: Int, offset: Int, document: Document, data: State): Boolean {
    val range = trimmedRange(document, TextRange(offset, document.getLineEndOffset(line)))
    return data.skipEmptyLines && range.isEmpty || lineCommentContext(line, offset, document, data) != null
  }

  override fun commentLine(line: Int, offset: Int, document: Document, data: State) {
    val end = trimmedRange(document, TextRange(offset, document.getLineEndOffset(line))).endOffset
    if (data.skipEmptyLines && offset == end) return
    val prefix = data.context.linePrefix
    if (prefix != null) {
      document.insertString(offset, prefix + if (data.lineSpace) " " else "")
    }
    else {
      if (lineCommentContext(line, offset, document, data) == MdxCommentContext.MDX) return
      SelfManagingCommenterUtil.insertBlockComment(
        offset, end, document,
        data.context.blockPrefix + if (data.blockSpace) " " else "",
        (if (data.blockSpace) " " else "") + data.context.blockSuffix,
      )
    }
  }

  override fun uncommentLine(line: Int, offset: Int, document: Document, data: State) {
    val context = lineCommentContext(line, offset, document, data) ?: return
    val prefix = context.linePrefix
    if (prefix != null) {
      var end = offset + prefix.length
      if (data.lineSpace && document.charsSequence.getOrNull(end) == ' ') end++
      document.deleteString(offset, end)
    }
    else {
      val end = trimmedRange(document, TextRange(offset, document.getLineEndOffset(line))).endOffset
      removeBlockComment(document, TextRange(offset, end), context, data.blockSpace)
    }
  }

  private fun lineCommentContext(line: Int, offset: Int, document: Document, data: State): MdxCommentContext? {
    val end = trimmedRange(document, TextRange(offset, document.getLineEndOffset(line))).endOffset
    if (isWrapped(document, TextRange(offset, end), MdxCommentContext.MDX)) return MdxCommentContext.MDX
    if (data.context == MdxCommentContext.JAVASCRIPT && CharArrayUtil.regionMatches(document.charsSequence, offset, "//")) {
      return MdxCommentContext.JAVASCRIPT
    }
    return null
  }

  override fun getBlockCommentPrefix(selectionStart: Int, document: Document, data: State): String = data.context.blockPrefix

  override fun getBlockCommentSuffix(selectionEnd: Int, document: Document, data: State): String = data.context.blockSuffix

  override fun getBlockCommentRange(selectionStart: Int, selectionEnd: Int, document: Document, data: State): TextRange? = data.blockComment

  override fun insertBlockComment(startOffset: Int, endOffset: Int, document: Document, data: State): TextRange {
    return SelfManagingCommenterUtil.insertBlockComment(
      startOffset, endOffset, document,
      data.context.blockPrefix + if (data.blockSpace) " " else "",
      (if (data.blockSpace) " " else "") + data.context.blockSuffix,
    )
  }

  override fun uncommentBlockComment(startOffset: Int, endOffset: Int, document: Document, data: State) {
    removeBlockComment(document, TextRange(startOffset, endOffset), data.context, data.blockSpace)
  }

  private fun removeBlockComment(document: Document, range: TextRange, context: MdxCommentContext, removeSpace: Boolean) {
    val prefixEnd = range.startOffset + context.blockPrefix.length
    val suffixStart = range.endOffset - context.blockSuffix.length
    val contentStart = prefixEnd + if (removeSpace && prefixEnd < suffixStart && document.charsSequence[prefixEnd] == ' ') 1 else 0
    val contentEnd = suffixStart - if (removeSpace && contentStart < suffixStart && document.charsSequence[suffixStart - 1] == ' ') 1 else 0
    document.deleteString(contentEnd, range.endOffset)
    document.deleteString(range.startOffset, contentStart)
  }

  private fun selectedBlockComment(document: Document, range: TextRange): Pair<MdxCommentContext, TextRange>? {
    val trimmed = trimmedRange(document, range)
    return MdxCommentContext.entries.firstOrNull { isWrapped(document, trimmed, it) }?.let { it to trimmed }
  }

  private fun commentAtCaret(file: PsiFile, document: Document, range: TextRange): Pair<MdxCommentContext, TextRange>? {
    val javascript = file.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return null
    val element = commonElement(javascript, range) ?: return null
    val comment = PsiTreeUtil.getParentOfType(element, PsiComment::class.java, false) ?: return null
    val commentRange = comment.textRange
    if (!isWrapped(document, commentRange, MdxCommentContext.JAVASCRIPT)) return null
    if (commentRange.startOffset > 0 && commentRange.endOffset < document.textLength) {
      val wrapper = TextRange(commentRange.startOffset - 1, commentRange.endOffset + 1)
      if (isWrapped(document, wrapper, MdxCommentContext.MDX)) return MdxCommentContext.MDX to wrapper
    }
    return MdxCommentContext.JAVASCRIPT to commentRange
  }

  private fun isWrapped(document: Document, range: TextRange, context: MdxCommentContext): Boolean {
    return range.length >= context.blockPrefix.length + context.blockSuffix.length &&
           CharArrayUtil.regionMatches(document.charsSequence, range.startOffset, context.blockPrefix) &&
           CharArrayUtil.regionMatches(document.charsSequence, range.endOffset - context.blockSuffix.length, context.blockSuffix)
  }

  private fun trimmedRange(document: Document, range: TextRange): TextRange {
    val text = document.charsSequence
    var start = range.startOffset
    var end = range.endOffset
    while (start < end && text[start].isWhitespace()) start++
    while (end > start && text[end - 1].isWhitespace()) end--
    return TextRange(start, end)
  }

  internal data class State(
    val context: MdxCommentContext,
    val lineSpace: Boolean,
    val blockSpace: Boolean,
    val blockComment: TextRange? = null,
    val skipEmptyLines: Boolean = false,
  ) : CommenterDataHolder()
}

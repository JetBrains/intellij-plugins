package com.intellij.mdx.backend.format

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.editor.tables.TableFormattingUtils
import org.intellij.plugins.markdown.lang.formatter.settings.MarkdownCustomCodeStyleSettings
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownTable

internal class MdxTablePostFormatProcessor : PostFormatProcessor {
  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    if (!shouldProcess(source.containingFile, settings)) {
      return source
    }
    val document = obtainDocument(source) ?: return source
    PsiDocumentManager.getInstance(source.project).commitDocument(document)
    val tables = if (source is MarkdownTable) {
      listOf(source)
    }
    else {
      PsiTreeUtil.findChildrenOfType(source, MarkdownTable::class.java)
    }
    for (table in tables) {
      processTable(table, document, settings)
      PsiDocumentManager.getInstance(source.project).commitDocument(document)
    }
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (!shouldProcess(source, settings)) {
      return rangeToReformat
    }
    val document = obtainDocument(source) ?: return rangeToReformat
    PsiDocumentManager.getInstance(source.project).commitDocument(document)
    for (table in PsiTreeUtil.findChildrenOfType(source, MarkdownTable::class.java)) {
      if (rangeToReformat.intersects(table.textRange)) {
        processTable(table, document, settings)
        PsiDocumentManager.getInstance(source.project).commitDocument(document)
      }
    }
    return source.textRange
  }

  private fun shouldProcess(file: PsiFile?, settings: CodeStyleSettings): Boolean {
    return file is MdxFile && settings.getCustomSettings(MarkdownCustomCodeStyleSettings::class.java).FORMAT_TABLES
  }

  private fun processTable(table: MarkdownTable, document: Document, settings: CodeStyleSettings) {
    val lineRange = getTableLineRange(table, document) ?: return
    val formattedText = formatAsMarkdownTable(table, document, lineRange, settings) ?: return
    val rangeStart = document.getLineStartOffset(lineRange.startLine)
    val rangeEnd = document.getLineEndOffset(lineRange.endLine)
    document.replaceString(rangeStart, rangeEnd, formattedText)
  }

  private fun obtainDocument(element: PsiElement): Document? {
    return element.containingFile?.viewProvider?.document
  }

  private fun getTableLineRange(table: MarkdownTable, document: Document): TableLineRange? {
    val range = table.textRange
    if (range.isEmpty) {
      return null
    }
    val startLine = document.getLineNumber(range.startOffset)
    val endLine = document.getLineNumber((range.endOffset - 1).coerceAtLeast(range.startOffset))
    return TableLineRange(startLine, endLine)
  }

  private fun formatAsMarkdownTable(table: MarkdownTable, document: Document, range: TableLineRange, settings: CodeStyleSettings): String? {
    val indent = getTableIndent(document, range)
    val text = (range.startLine..range.endLine).joinToString("\n") { line ->
      val lineStart = document.getLineStartOffset(line)
      val lineEnd = document.getLineEndOffset(line)
      val lineText = document.charsSequence.subSequence(lineStart, lineEnd).toString()
      lineText.drop(getLineIndent(document, line).length)
    }
    val tableStyle = settings.getCustomSettings(MarkdownCustomCodeStyleSettings::class.java).tableStyle
    val markdownFile = MarkdownPsiElementFactory.createFile(table.project, text)
    val markdownTable = PsiTreeUtil.findChildOfType(markdownFile, MarkdownTable::class.java) ?: return null
    val markdownDocument = markdownFile.viewProvider.document ?: return null
    TableFormattingUtils.reformatAllColumns(markdownTable, markdownDocument, tableStyle, trimToMaxContent = true)
    return markdownDocument.text.removeSuffix("\n").lines().joinToString("\n") { "$indent$it" }
  }

  private fun getTableIndent(document: Document, range: TableLineRange): String {
    val indents = (range.startLine..range.endLine).map { getLineIndent(document, it) }
    return indents.firstOrNull().takeUnless { it.isNullOrEmpty() } ?: indents.firstOrNull { it.isNotEmpty() }.orEmpty()
  }

  private fun getLineIndent(document: Document, line: Int): String {
    val lineStart = document.getLineStartOffset(line)
    val lineEnd = document.getLineEndOffset(line)
    var offset = lineStart
    while (offset < lineEnd && document.charsSequence[offset].isWhitespace()) {
      offset++
    }
    return document.charsSequence.subSequence(lineStart, offset).toString()
  }

  private data class TableLineRange(val startLine: Int, val endLine: Int)
}

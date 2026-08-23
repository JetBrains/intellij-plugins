package com.intellij.mdx.backend.js

import com.intellij.lang.ecmascript6.actions.ES6AddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * MDX-aware ES6 import inserter. The platform anchor search treats a leading front matter header as part
 * of a single leading `OUTER_BLOCK` and inserts a new import *before* it, pushing the opening delimiter
 * off offset 0 and breaking the document (front matter is only recognized when it starts the file). This
 * lets the platform insert normally, then relocates any import/export lines left before the header to
 * right after it.
 */
internal class MdxAddImportExecutor(place: PsiElement) : ES6AddImportExecutor(place) {

  // Captured pre-insert: only a file that already had front matter at offset 0 can have it displaced by
  // the platform's insertion, so this guards against misclassifying a body `---`/`+++` thematic break.
  private val originalHadFrontMatter: Boolean =
    PsiDocumentManager.getInstance(place.project).getDocument(place.containingFile)
      ?.let { startsWithFrontMatterDelimiter(it.charsSequence) } == true

  override fun postProcessScope(place: PsiElement, info: JSImportDescriptor, scope: PsiElement) {
    super.postProcessScope(place, info, scope)
    moveLeadingEsmAfterFrontMatter(scope)
  }

  private fun moveLeadingEsmAfterFrontMatter(scope: PsiElement) {
    if (!originalHadFrontMatter) return
    val project = scope.project
    val manager = PsiDocumentManager.getInstance(project)
    val document = manager.getDocument(scope.containingFile) ?: return

    runUndoTransparentWriteAction {
      // Flush any pending PSI edits (e.g. the just-inserted import) into the document before reading it.
      manager.doPostponedOperationsAndUnblockDocument(document)

      val text = document.charsSequence.toString()
      val relocation = computeRelocation(text) ?: return@runUndoTransparentWriteAction

      // Remove the leading ESM prefix first, then insert it after the front matter block.
      document.deleteString(relocation.prefixStart, relocation.prefixEnd)
      val insertAt = relocation.insertAt - (relocation.prefixEnd - relocation.prefixStart)
      document.insertString(insertAt, relocation.prefixText)
      manager.commitDocument(document)
    }
  }

  private data class Relocation(val prefixStart: Int, val prefixEnd: Int, val prefixText: String, val insertAt: Int)

  /** Where to move a displaced `<leading import/export lines>` + `<front matter header>`, or null if fine. */
  private fun computeRelocation(text: CharSequence): Relocation? {
    val lines = splitLines(text)
    if (lines.isEmpty()) return null

    var openingIndex = -1
    for (index in lines.indices) {
      val content = lines[index].content
      if (isOpeningDelimiterLine(content)) {
        if (index == 0) return null // already valid front matter, nothing to fix
        if (lines.subList(0, index).all { it.content.isBlank() || isEsmLine(it.content) }) {
          openingIndex = index
        }
        break
      }
      if (!(content.isBlank() || isEsmLine(content))) return null // real content before any header -> leave as is
    }
    if (openingIndex <= 0) return null

    // The displaced prefix must contain at least one ESM line, otherwise there is nothing to move.
    val prefixLines = lines.subList(0, openingIndex)
    if (prefixLines.none { isEsmLine(it.content) }) return null

    val opening = lines[openingIndex].content
    val closingIndex = (openingIndex + 1 until lines.size).firstOrNull { isClosingDelimiterLine(opening, lines[it].content) } ?: return null

    val prefixStart = lines[0].start
    val prefixEnd = lines[openingIndex].start
    val prefixText = text.subSequence(prefixStart, prefixEnd).toString()

    // If an existing import/export block already follows (possibly after blank separator lines), group
    // the relocated import with it. Otherwise insert right at the closing delimiter's line end, i.e.
    // immediately after it, leaving any blank lines before the body untouched.
    var insertLine = closingIndex + 1
    while (insertLine < lines.size && lines[insertLine].content.isBlank()) {
      insertLine++
    }
    val insertAt = when {
      insertLine < lines.size && isEsmLine(lines[insertLine].content) -> lines[insertLine].start
      else -> lines[closingIndex].end
    }
    return Relocation(prefixStart, prefixEnd, prefixText, insertAt)
  }

  private data class Line(val start: Int, val end: Int, val content: String)

  private fun splitLines(text: CharSequence): List<Line> {
    val lines = mutableListOf<Line>()
    var lineStart = 0
    var offset = 0
    while (offset < text.length) {
      if (text[offset] == '\n') {
        lines.add(Line(lineStart, offset + 1, text.subSequence(lineStart, offset).toString()))
        lineStart = offset + 1
      }
      offset++
    }
    if (lineStart < text.length) {
      lines.add(Line(lineStart, text.length, text.subSequence(lineStart, text.length).toString()))
    }
    return lines
  }

  private fun isEsmLine(content: String): Boolean {
    val trimmed = content.trimStart()
    return startsWithKeyword(trimmed, "import") || startsWithKeyword(trimmed, "export")
  }

  private fun startsWithKeyword(text: String, keyword: String): Boolean {
    if (!text.startsWith(keyword)) return false
    val next = text.getOrNull(keyword.length) ?: return true
    return !(next.isLetterOrDigit() || next == '_' || next == '$')
  }

  // Mirrors org.intellij.plugins.markdown...FrontMatterHeaderMarkerProvider delimiter rules.
  private fun isOpeningDelimiterLine(line: String): Boolean = isYamlDashes(line) || isTomlPluses(line)

  /** True when [text] begins (at offset 0, the file's first line) with a YAML/TOML front matter opener. */
  private fun startsWithFrontMatterDelimiter(text: CharSequence): Boolean {
    val end = text.indexOf('\n').let { if (it < 0) text.length else it }
    return isOpeningDelimiterLine(text.subSequence(0, end).toString())
  }

  private fun isClosingDelimiterLine(opening: String, closing: String): Boolean {
    if (isYamlDashes(opening)) return isYamlDashes(closing) || isYamlDots(closing)
    if (isTomlPluses(opening)) return isTomlPluses(closing)
    return false
  }

  private fun isYamlDashes(line: String): Boolean = line.length >= 3 && line.all { it == '-' }
  private fun isYamlDots(line: String): Boolean = line.length >= 3 && line.all { it == '.' }
  private fun isTomlPluses(line: String): Boolean = line.length >= 3 && line.all { it == '+' }
}

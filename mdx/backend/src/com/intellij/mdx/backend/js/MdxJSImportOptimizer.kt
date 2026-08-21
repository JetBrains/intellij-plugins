package com.intellij.mdx.backend.js

import com.intellij.lang.ImportOptimizer
import com.intellij.lang.LanguageImportStatements
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.parse.MdxEsmScanner

internal class MdxJSImportOptimizer : ImportOptimizer {
  override fun supports(file: PsiFile): Boolean {
    return file.language == MdxJSLanguage.INSTANCE
  }

  override fun processFile(file: PsiFile): Runnable {
    val delegate = findJavaScriptOptimizer(file)?.processFile(file)
    return Runnable {
      delegate?.run()
      repairAdjacentEsmStatements(file)
    }
  }

  private fun findJavaScriptOptimizer(file: PsiFile): ImportOptimizer? {
    return LanguageImportStatements.INSTANCE
      .allForLanguageOrAny(JavascriptLanguage)
      .firstOrNull { it !is MdxJSImportOptimizer && it.supports(file) }
  }

  private fun repairAdjacentEsmStatements(file: PsiFile) {
    val manager = PsiDocumentManager.getInstance(file.project)
    val document = manager.getDocument(file) ?: return
    val offsets = collectMissingSeparatorOffsets(document.charsSequence.toString())
    for (offset in offsets.asReversed()) {
      document.insertString(offset, "\n")
    }
    if (offsets.isNotEmpty()) {
      manager.commitDocument(document)
    }
  }

  private fun collectMissingSeparatorOffsets(text: CharSequence): List<Int> {
    val offsets = mutableListOf<Int>()
    var offset = 0
    while (offset < text.length) {
      if (MdxEsmScanner.isLineStart(text, offset)) {
        val blockEnd = MdxEsmScanner.scanBlock(text, offset)?.range?.last ?: text.length
        collectMissingSeparatorOffsets(text, offset, blockEnd, offsets)
        offset = blockEnd.coerceAtLeast(offset + 1)
      }
      else {
        offset++
      }
    }
    return offsets
  }

  private fun collectMissingSeparatorOffsets(text: CharSequence, start: Int, end: Int, result: MutableList<Int>) {
    var offset = start + esmKeywordLength(text, start)
    var parenDepth = 0
    var braceDepth = 0
    var bracketDepth = 0
    while (offset < end) {
      if (parenDepth == 0 && braceDepth == 0 && bracketDepth == 0 && isEsmKeywordAt(text, offset) && !hasSeparatorBefore(text, offset)) {
        result.add(offset)
        offset += esmKeywordLength(text, offset)
        continue
      }

      when (text[offset]) {
        '\'' -> offset = scanQuoted(text, offset, end, '\'')
        '"' -> offset = scanQuoted(text, offset, end, '"')
        '`' -> offset = scanTemplate(text, offset, end)
        '/' -> offset = scanSlash(text, offset, end)
        '(' -> {
          parenDepth++
          offset++
        }
        ')' -> {
          if (parenDepth > 0) parenDepth--
          offset++
        }
        '{' -> {
          braceDepth++
          offset++
        }
        '}' -> {
          if (braceDepth > 0) braceDepth--
          offset++
        }
        '[' -> {
          bracketDepth++
          offset++
        }
        ']' -> {
          if (bracketDepth > 0) bracketDepth--
          offset++
        }
        else -> offset++
      }
    }
  }

  private fun scanQuoted(text: CharSequence, start: Int, end: Int, quote: Char): Int {
    var offset = start + 1
    while (offset < end) {
      when (text[offset]) {
        '\\' -> offset += 2
        quote -> return offset + 1
        else -> offset++
      }
    }
    return end
  }

  private fun scanTemplate(text: CharSequence, start: Int, end: Int): Int {
    var offset = start + 1
    while (offset < end) {
      when (text[offset]) {
        '\\' -> offset += 2
        '`' -> return offset + 1
        else -> offset++
      }
    }
    return end
  }

  private fun scanSlash(text: CharSequence, start: Int, end: Int): Int {
    if (text.getOrNull(start + 1) == '/') {
      var offset = start + 2
      while (offset < end && text[offset] != '\n') {
        offset++
      }
      return offset
    }
    if (text.getOrNull(start + 1) == '*') {
      var offset = start + 2
      while (offset + 1 < end) {
        if (text.startsWith("*/", offset, ignoreCase = false)) return offset + 2
        offset++
      }
      return end
    }
    return start + 1
  }

  private fun hasSeparatorBefore(text: CharSequence, offset: Int): Boolean {
    val previous = text.getOrNull(offset - 1) ?: return true
    return previous.isWhitespace() || previous == ';'
  }

  private fun isEsmKeywordAt(text: CharSequence, offset: Int): Boolean {
    return keywordAt(text, offset, "import") || keywordAt(text, offset, "export")
  }

  private fun esmKeywordLength(text: CharSequence, offset: Int): Int {
    return if (keywordAt(text, offset, "import")) "import".length else "export".length
  }

  private fun keywordAt(text: CharSequence, offset: Int, keyword: String): Boolean {
    if (offset + keyword.length > text.length) return false
    if (keyword.indices.any { text[offset + it] != keyword[it] }) return false
    val before = text.getOrNull(offset - 1)
    val after = text.getOrNull(offset + keyword.length)
    return before?.let { !isNamePart(it) } != false && after?.let { !isNamePart(it) } != false
  }

  private fun isNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }
}

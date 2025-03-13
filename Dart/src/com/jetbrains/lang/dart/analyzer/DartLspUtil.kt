// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.analyzer

import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtilRt
import org.dartlang.analysis.server.protocol.DartLspPosition
import org.dartlang.analysis.server.protocol.DartLspTextEdit
import kotlin.math.min

fun getOffsetInDocument(document: Document, position: DartLspPosition): Int? {
  val lineCount = document.lineCount
  val line = position.line
  var character = position.character
  if (line == lineCount && character == 0) return document.textLength
  if (line < 0 || line >= lineCount || character < 0) return null

  val lineStartOffset = document.getLineStartOffset(line)
  if (line + 1 < lineCount && character > 0) {
    // Make sure that the `character` value doesn't exceed the line length.
    val nextLineStartOffset = document.getLineStartOffset(line + 1)
    character = min(character, nextLineStartOffset - 1 - lineStartOffset)
  }
  else if (line + 1 == lineCount && character > 0) {
    // workaround for servers that send { "line": <last_line>, "character": 2147483647 }
    character = min(character, document.textLength - lineStartOffset)
  }

  return (lineStartOffset + character).let { if (it <= document.textLength) it else null }
}

fun applyTextEdits(document: Document, textEdits: List<DartLspTextEdit>): Boolean {
  textEdits
    // descending sorting needed to apply edits starting from the end of the document, so the edits they don't influence each other
    .sortedWith { edit1, edit2 ->
      (edit2.range.start.line - edit1.range.start.line).takeIf { it != 0 }
      ?: (edit2.range.start.character - edit1.range.start.character)
    }
    .forEach { if (!applyTextEdit(document, it)) return@applyTextEdits false }

  return true
}

/**
 * @return `true` if `textEdit` was applied successfully;
 * `false` if the `textEdit` can't be applied to the `document` because `textEdit.range` is outside the `document` text range
 */
fun applyTextEdit(document: Document, textEdit: DartLspTextEdit): Boolean {
  val startOffset = getOffsetInDocument(document, textEdit.range.start)
  val endOffset = getOffsetInDocument(document, textEdit.range.end)
  if (startOffset == null || endOffset == null) {
    fileLogger().warn("Ignoring TextEdit, its text range is outside the document text range.\n" +
                      "document.lineCount = ${document.lineCount}, document.textLength = ${document.textLength}, range: ${textEdit.range}")
    return false
  }

  val newText = StringUtilRt.convertLineSeparators(textEdit.newText)
  document.replaceString(startOffset, endOffset, newText)
  return true
}

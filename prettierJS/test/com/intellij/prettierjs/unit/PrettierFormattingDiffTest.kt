// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.TextRange
import com.intellij.prettierjs.formatting.CaretSnapshot
import com.intellij.prettierjs.formatting.PrettierFormattingApplier
import com.intellij.prettierjs.formatting.applyTextDifferencesToDocument
import com.intellij.prettierjs.formatting.computeFormattingDiff
import com.intellij.prettierjs.formatting.createFormattingContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Checks the document changes that [computeFormattingDiff] makes on the line of the caret.
 * That line gets a diff by chars, so that the caret markers keep their place.
 * The diff must stay short, because each change runs every document listener.
 */
class PrettierFormattingDiffTest : BasePlatformTestCase() {

  fun testOneLineJsonTakesFewChanges() {
    val original = oneLineJson(500)
    val formatted = formattedJson(500)
    val caret = original.indexOf("\"id\":250") + "\"id\":25".length

    val result = applyFormatting(original, formatted, caret)

    assertEquals(formatted, result.text)
    assertTrue("changes: ${result.changes}", result.changes <= 3)
    assertEquals(formatted.indexOf("\"id\": 250") + "\"id\": 25".length, result.caret)
  }

  /**
   * Reproduces WEB-80318 in an editor. Each document change runs the editor listeners over the whole document.
   * With one change per formatted line, a one-line JSON of this size froze the EDT for about a minute.
   */
  fun testOneLineJsonReformatInEditorIsFast() {
    val original = oneLineJson(2000)
    val formatted = formattedJson(2000)
    val caret = original.indexOf("\"id\":1000") + "\"id\":100".length
    myFixture.configureByText("big.json", original)
    myFixture.editor.caretModel.moveToOffset(caret)
    val document = myFixture.editor.document
    val applier = PrettierFormattingApplier.from(document, myFixture.file, formatted)

    val changes = countChanges(document)
    val start = System.nanoTime()
    WriteCommandAction.runWriteCommandAction(project) { applier.apply(project, myFixture.file) }
    val millis = (System.nanoTime() - start) / 1_000_000

    assertEquals(formatted, document.text)
    assertEquals(formatted.indexOf("\"id\": 1000") + "\"id\": 100".length, myFixture.editor.caretModel.offset)
    assertTrue("${changes.count} document changes", changes.count <= 3)
    assertTrue("the apply took $millis ms", millis < 10_000)
  }

  fun testCaretStaysBeforeInsertedText() {
    val result = applyFormatting("a  =  b", "a = b;\n", caret = 7)

    assertEquals("a = b;\n", result.text)
    assertEquals(5, result.caret)
  }

  fun testCaretInRemovedSpaceKeepsPreviousToken() {
    val result = applyFormatting("foo(     )", "foo();\n", caret = 6, previousToken = TextRange(3, 4))

    assertEquals("foo();\n", result.text)
    assertNull(result.caret)
    assertEquals(4, result.previousTokenEnd)
  }

  private class FormattingResult(val text: String, val changes: Int, val caret: Int?, val previousTokenEnd: Int?)

  private fun applyFormatting(original: String, formatted: String, caret: Int, previousToken: TextRange? = null): FormattingResult {
    val document = EditorFactory.getInstance().createDocument(original)
    val primary = document.createRangeMarker(caret, caret)
    val secondary = previousToken?.let { document.createRangeMarker(it) }
    val context = createFormattingContext(document, formatted)
    val diff = computeFormattingDiff(context, CaretSnapshot(primary, secondary))

    val changes = countChanges(document)
    WriteCommandAction.runWriteCommandAction(project) { applyTextDifferencesToDocument(context, diff) }

    return FormattingResult(
      text = document.text,
      changes = changes.count,
      caret = primary.takeIf { it.isValid }?.startOffset,
      previousTokenEnd = secondary?.takeIf { it.isValid }?.endOffset,
    )
  }

  private class ChangeCounter : DocumentListener {
    var count = 0

    override fun documentChanged(event: DocumentEvent) {
      count++
    }
  }

  private fun countChanges(document: Document): ChangeCounter {
    val counter = ChangeCounter()
    document.addDocumentListener(counter, testRootDisposable)
    return counter
  }

  /** A JSON array of [items] small objects on one line, as an HTTP client response is. */
  private fun oneLineJson(items: Int): String =
    (1..items).joinToString(",", "[", "]") { """{"id":$it,"name":"item $it","tags":["a","b"],"ok":true}""" }

  /** The [oneLineJson] of [items] objects with one object per line, as Prettier formats it. */
  private fun formattedJson(items: Int): String =
    (1..items).joinToString(",\n", "[\n", "\n]\n") { """  {"id": $it, "name": "item $it", "tags": ["a", "b"], "ok": true}""" }
}

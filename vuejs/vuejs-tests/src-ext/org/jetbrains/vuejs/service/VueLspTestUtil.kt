// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.lsp.tests.checkLspHighlightingForData
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

/**
 * Creates [ExpectedHighlightingData] from the given [text], waits for the `textDocument/publishDiagnostics` notification
 * from the LSP server, and checks that the errors/warnings highlighting for the current file match the expected result.
 * Then verifies that the current document content matches the expected text (without markers).
 *
 * @see [checkLspHighlightingForData].
 */
fun CodeInsightTestFixture.checkLspHighlightingByText(text: String, withWarnings: Boolean) {
  val document = EditorFactory.getInstance().createDocument(StringUtil.convertLineSeparators(text))
  val data = ExpectedHighlightingData(document, withWarnings, withWarnings, false)
  data.init()
  checkLspHighlightingForData(data)
  checkResult(document.text)
}
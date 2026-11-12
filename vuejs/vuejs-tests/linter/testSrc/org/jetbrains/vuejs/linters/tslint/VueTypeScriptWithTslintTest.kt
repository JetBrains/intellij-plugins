// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.linter.tslint.TypeScriptServiceWithTslintFixture.Companion.typeScriptServiceWithTslintFixture
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.LineSeparator
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class VueTypeScriptWithTslintTest {
  private val tslint by typeScriptServiceWithTslintFixture(vueRelativeTestDataPath() + "/linters/tslint/")

  @Test
  fun testMatchingLineEndingsNotHighlighted() {
    tslint.doHighlightingTest("main", "vue") {
      JSTestUtils.ensureLineSeparators(tslint.fixture.file, LineSeparator.CRLF)
    }
  }

  @Test
  fun testMismatchedLineEndingsHighlighted() {
    tslint.doHighlightingTest("main", "vue") {
      JSTestUtils.ensureLineSeparators(tslint.fixture.file, LineSeparator.LF)
    }
  }

  @Test
  fun testFixAllWithUpdatingLineSeparators() {
    doFixAllTest()
    // saveAllDocuments needs the EDT.
    timeoutRunBlocking { edtWriteAction { FileDocumentManager.getInstance().saveAllDocuments() } }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(tslint.fixture.file.virtualFile)))
  }

  @Test
  fun testFixAllWithCrlfLineSeparator() {
    tslint.doFixTest("main", "vue", "Fix all auto-fixable tslint failures") {
      JSTestUtils.ensureLineSeparators(tslint.fixture.file, LineSeparator.CRLF)
    }
    // saveAllDocuments needs the EDT.
    timeoutRunBlocking { edtWriteAction { FileDocumentManager.getInstance().saveAllDocuments() } }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(tslint.fixture.file.virtualFile)))
  }

  private fun doFixAllTest() {
    tslint.doFixTest("main", "vue", "Fix all auto-fixable tslint failures")
  }
}

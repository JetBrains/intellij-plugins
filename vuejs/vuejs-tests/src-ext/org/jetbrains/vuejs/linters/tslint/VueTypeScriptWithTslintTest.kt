// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.linter.tslint.TypeScriptServiceWithTslintTestBase
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.LineSeparator
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath

class VueTypeScriptWithTslintTest : TypeScriptServiceWithTslintTestBase() {

  override fun getBasePath(): String {
    return vueRelativeTestDataPath() + "/linters/tslint/"
  }

  fun testFilterWhitespaceErrorsByScriptTag() {
    doHighlightingTest("main", "vue")
  }

  fun testFixAllErrorsWithWhitespaceRules() {
    doFixAllTest()
  }

  fun testMatchingLineEndingsNotHighlighted() {
    doHighlightingTest("main", "vue") {
      JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.CRLF)
    }
  }

  fun testMismatchedLineEndingsHighlighted() {
    doHighlightingTest("main", "vue") {
      JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.LF)
    }
  }

  fun testFixAllWithUpdatingLineSeparators() {
    doFixAllTest()
    FileDocumentManager.getInstance().saveAllDocuments()
    TestCase.assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(file.virtualFile)))
  }

  fun testFixAllWithCrlfLineSeparator() {
    doFixTest("main", "vue", "Fix all auto-fixable tslint failures") {
      JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.CRLF)
    }
    FileDocumentManager.getInstance().saveAllDocuments()
    TestCase.assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(file.virtualFile)))
  }

  private fun doFixAllTest() {
    doFixTest("main", "vue", "Fix all auto-fixable tslint failures")
  }
}

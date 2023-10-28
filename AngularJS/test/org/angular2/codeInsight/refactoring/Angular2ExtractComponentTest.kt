// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil.RefactoringErrorHintException
import com.intellij.testFramework.UsefulTestCase
import org.angular2.Angular2TestCase

class Angular2ExtractComponentTest : Angular2TestCase("refactoring/extractComponent") {

  fun testSingleElementMultiLineFromCaret() {
    doMultiFileTest()
  }

  fun testSingleElementSingleLine() {
    doMultiFileTest()
  }

  fun testMultiElement() {
    doMultiFileTest()
  }

  fun testNoElement() {
    doMultiFileTest()
  }

  fun testNameClashes() {
    doMultiFileTest()
  }

  fun testExtractFromInlineTemplate() {
    doMultiFileTest("src/app/app.component.ts")
  }

  fun testUnsupportedSelection() {
    doFailedTest()
  }

  fun testUnsupportedSelection2() {
    doFailedTest()
  }

  fun testUnsupportedSelection3() {
    doFailedTest()
  }

  fun testUnsupportedSelection4() {
    doFailedTest()
  }

  private fun doMultiFileTest(source: String = "src/app/app.component.html") {
    doConfiguredTest(dir = true, checkResult = true, configureFileName = source) {
      myFixture.performEditorAction("Angular2ExtractComponentAction")
    }
  }

  private fun doFailedTest() {
    UsefulTestCase.assertThrows(RefactoringErrorHintException::class.java) { doMultiFileTest() }
  }

}

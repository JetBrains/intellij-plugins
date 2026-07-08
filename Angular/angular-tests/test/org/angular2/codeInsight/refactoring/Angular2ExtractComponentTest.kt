// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.refactoring.util.CommonRefactoringUtil.RefactoringErrorHintException
import com.intellij.testFramework.UsefulTestCase
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2ExtractComponentTest : Angular2TestCase("refactoring/extractComponent") {

  // TODO WEB-67260 - fails on server
  fun _testSingleElementMultiLineFromCaret() {
    doMultiFileTest()
  }

  @Test
  fun testSingleElementSingleLine() {
    doMultiFileTest()
  }

  // TODO WEB-67260 - fails on server
  fun _testMultiElement() {
    doMultiFileTest()
  }

  @Test
  fun testNoElement() {
    doMultiFileTest()
  }

  @Test
  fun testNameClashes() {
    doMultiFileTest()
  }

  @Test
  fun testExtractFromInlineTemplate() {
    doMultiFileTest("src/app/app.component.ts")
  }

  @Test
  fun testUnsupportedSelection() {
    doFailedTest()
  }

  @Test
  fun testUnsupportedSelection2() {
    doFailedTest()
  }

  @Test
  fun testUnsupportedSelection3() {
    doFailedTest()
  }

  @Test
  fun testUnsupportedSelection4() {
    doFailedTest()
  }

  private fun doMultiFileTest(source: String = "src/app/app.component.html") {
    doConfiguredTest(Angular2TestModule.TS_LIB,
                     Angular2TestModule.ANGULAR_CORE_16_2_8,
                     Angular2TestModule.ANGULAR_COMMON_16_2_8,
                     Angular2TestModule.ANGULAR_FORMS_16_2_8,
                     dir = true, checkResult = true, configureFileName = source,
                     configurators = listOf(Angular2TsConfigFile(strictTemplates = true))) {
      myFixture.performEditorAction("Angular2ExtractComponentAction")
    }
  }

  private fun doFailedTest() {
    UsefulTestCase.assertThrows(RefactoringErrorHintException::class.java) { doMultiFileTest() }
  }

}

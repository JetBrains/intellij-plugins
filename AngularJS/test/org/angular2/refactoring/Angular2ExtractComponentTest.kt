// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.refactoring.util.CommonRefactoringUtil.RefactoringErrorHintException
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2ExtractComponentTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "extractComponent"
  }

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
    doTest { rootDir: VirtualFile?, rootAfter: VirtualFile? ->
      myFixture.configureFromTempProjectFile(source)
      myFixture.performEditorAction("Angular2ExtractComponentAction")
    }
  }

  private fun doFailedTest() {
    UsefulTestCase.assertThrows(RefactoringErrorHintException::class.java) { doMultiFileTest() }
  }

  override fun getTestRoot(): String {
    return "/"
  }

  override fun createTempDirTestFixture(): TempDirTestFixture {
    val policy = IdeaTestExecutionPolicy.current()
    return if (policy != null) policy.createTempDirTestFixture() else LightTempDirTestFixtureImpl(false)
  }
}

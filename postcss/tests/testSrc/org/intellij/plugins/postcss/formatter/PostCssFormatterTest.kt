package org.intellij.plugins.postcss.formatter

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.testNameFixture
import org.intellij.plugins.postcss.postCssFixture
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/postcss/tests/testData/formatter")
class PostCssFormatterTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val postCss by postCssFixture(projectFixture)
  private val testName by testNameFixture(lowerCaseFirstLetter = false)

  private fun doTest() {
    val myFixture = postCss.fixture
    myFixture.configureByFile("$testName.pcss")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
    myFixture.checkResultByFile("${testName}_after.pcss")
  }

  @Test
  fun testLineComment(): Unit = doTest()
}

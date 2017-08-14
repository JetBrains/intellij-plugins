package org.intellij.plugins.markdown.completion

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.intellij.plugins.markdown.MarkdownTestingUtil

class HeaderReferenceCompletionTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/completion/headerAnchor/"
  }

  fun testHeader1() {
    doTest()
  }

  fun testHeader2() {
    doTest()
  }

  fun testInBullet() {
    doTest()
  }

  fun testMultipleHeaders() {
    myFixture.testCompletionVariants(getBeforeFileName(),
                                     "#environment-variables",
                                     "#how-do-i-get-set-up",
                                     "#mxbezier3scalar",
                                     "#plugin-list",
                                     "#requirements",
                                     "#what-is-this-repository-for")
  }

  private fun getBeforeFileName() = getTestName(true) + ".md"

  private fun doTest() {
    myFixture.testCompletion(getBeforeFileName(), getTestName(true) + "_after.md")
  }


}
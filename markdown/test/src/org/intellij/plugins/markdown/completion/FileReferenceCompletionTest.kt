package org.intellij.plugins.markdown.completion

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.intellij.plugins.markdown.MarkdownTestingUtil

class FileReferenceCompletionTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/completion/fileReference/"
  }

  fun testRelativePath() {
    myFixture.testCompletion("relativePath.md", "relativePath_after.md")
  }
}

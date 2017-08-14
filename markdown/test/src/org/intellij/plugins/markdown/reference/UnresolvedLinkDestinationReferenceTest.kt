package org.intellij.plugins.markdown.reference

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.intellij.plugins.markdown.MarkdownTestingUtil
import org.intellij.plugins.markdown.lang.references.MarkdownUnresolvedFileReferenceInspection

class UnresolvedLinkDestinationReferenceTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String = MarkdownTestingUtil.TEST_DATA_PATH + "/reference/linkDestination/"

  fun testUnresolvedReference() {
    myFixture.enableInspections(MarkdownUnresolvedFileReferenceInspection::class.java)
    myFixture.testHighlighting(true, false, false, "sample_unresolved.md")
  }

  override fun tearDown() {
    try {
      myFixture.disableInspections(MarkdownUnresolvedFileReferenceInspection())
    }
    finally {
      super.tearDown()
    }
  }
}
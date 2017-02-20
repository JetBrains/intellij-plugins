import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class MakefileHighlightingTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testUnresolved() = doTest()

  fun doTest() { myFixture.testHighlighting("$testDataPath/$basePath/${getTestName(true)}.mk") }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "highlighting"
}
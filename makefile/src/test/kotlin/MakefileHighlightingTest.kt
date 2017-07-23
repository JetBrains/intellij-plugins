import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class MakefileHighlightingTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testUnresolved() = doTest()
  fun testRedundant() = doTest(true)
  fun testTargetspecificvars() = doTest()

  fun doTest(checkInfos: Boolean = false) { myFixture.testHighlighting(true, checkInfos, true, "$testDataPath/$basePath/${getTestName(true)}.mk") }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "highlighting"
}
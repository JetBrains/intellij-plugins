import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class MakefileCompletionTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() = doTest("b", "c", "d", "${getTestName(true)}.mk")
  fun testTargets() = doTest("a", "${getTestName(true)}.mk")

  fun doTest(vararg variants: String) = myFixture.testCompletionVariants("$testDataPath/$basePath/${getTestName(true)}.mk", *variants)

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "completion"
}
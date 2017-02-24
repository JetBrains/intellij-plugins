import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class MakefileRemoveRuleQuickfixTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSingle() = doTest()
  fun testTwo() = doTest()

  fun doTest() {
    myFixture.configureByFile("$testDataPath/$basePath/${getTestName(true)}.mk")
    val intention = myFixture.findSingleIntention("Remove Empty Rule")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "quickfix/removeRule"
}
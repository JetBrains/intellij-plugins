import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class MakefileCreateRuleQuickfixTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() {
    myFixture.configureByFile("$testDataPath/$basePath/${getTestName(true)}.mk")
    val intention = myFixture.findSingleIntention("Create Rule")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "quickfix/createRule"
}
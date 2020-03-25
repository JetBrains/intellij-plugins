import com.intellij.testFramework.fixtures.*

class MakefileRemoveRuleQuickfixTest : BasePlatformTestCase() {
  fun testSingle() = doTest()
  fun testTwo() = doTest()

  fun doTest() {
    myFixture.configureByFile("$basePath/${getTestName(true)}.mk")
    val intention = myFixture.findSingleIntention("Remove Empty Rule")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "quickfix/removeRule"
}
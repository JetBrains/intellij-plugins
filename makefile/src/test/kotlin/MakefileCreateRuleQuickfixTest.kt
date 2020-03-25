import com.intellij.testFramework.fixtures.*

class MakefileCreateRuleQuickfixTest : BasePlatformTestCase() {
  fun testSimple() = doTest()
  fun testMiddle() = doTest()


  fun doTest() {
    myFixture.configureByFile("$basePath/${getTestName(true)}.mk")
    val intention = myFixture.findSingleIntention("Create Rule")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "quickfix/createRule"
}
import com.intellij.testFramework.fixtures.*

class MakefileFoldingTest : BasePlatformTestCase() {
  fun testRule() = doTest()
  fun testVariable() = doTest()
  fun testDefine() = doTest()


  fun doTest() = myFixture.testFolding("$testDataPath/$basePath/${getTestName(true)}.mk")

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "folding"
}
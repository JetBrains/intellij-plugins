import com.intellij.testFramework.*
import com.intellij.testFramework.fixtures.*

class MakefileStructureViewTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() {
    val filename = "${getTestName(true)}.mk"
    myFixture.configureByFile("$testDataPath/$basePath/$filename")
    myFixture.testStructureView {
      PlatformTestUtil.expandAll(it.tree)
      PlatformTestUtil.assertTreeEqual(it.tree, "-simple.mk\n all\n hello\n world\n")
    }
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "structure"
}
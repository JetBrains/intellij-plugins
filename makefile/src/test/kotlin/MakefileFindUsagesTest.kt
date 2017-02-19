import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Assert.assertThat

class MakefileFindUsagesTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() {
    val usages = myFixture.testFindUsages("$testDataPath/$basePath/${getTestName(true)}.mk")

    assertThat(usages, hasSize(2))
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "findUsages"
}
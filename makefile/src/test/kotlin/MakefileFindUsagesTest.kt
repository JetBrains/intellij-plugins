import com.intellij.codeInsight.TargetElementUtil
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.hamcrest.core.IsNull.nullValue
import org.junit.Assert.assertThat

class MakefileFindUsagesTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() {
    val usages = myFixture.testFindUsages("$testDataPath/$basePath/${getTestName(true)}.mk")

    assertThat(usages, hasSize(2))
  }

  fun testPhony() = notSearchableForUsages()
  fun testForce() = notSearchableForUsages()

  fun notSearchableForUsages() {
    myFixture.configureByFiles("$testDataPath/$basePath/${getTestName(true)}.mk")
    val targetElement = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
    val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)

    assertThat(handler, nullValue())
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "findUsages"
}
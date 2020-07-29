import com.intellij.codeInsight.*
import com.intellij.find.*
import com.intellij.find.impl.*
import com.intellij.testFramework.fixtures.*
import org.hamcrest.core.IsNull.*
import org.junit.Assert.*

class MakefileFindUsagesTest : BasePlatformTestCase() {
  fun testSimple() {
    val usages = myFixture.testFindUsages("$basePath/${getTestName(true)}.mk")

    assertEquals(2, usages.size)
  }

  fun testPhony() = notSearchableForUsages()
  fun testForce() = notSearchableForUsages()

  fun notSearchableForUsages() {
    myFixture.configureByFiles("$basePath/${getTestName(true)}.mk")
    val targetElement = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
    val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)

    assertThat(handler, nullValue())
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "findUsages"
}
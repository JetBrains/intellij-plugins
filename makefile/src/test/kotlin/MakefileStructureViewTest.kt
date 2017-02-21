import com.intellij.ide.structureView.newStructureView.StructureViewComponent
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import name.kropp.intellij.makefile.MakefileStructureViewElement
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat

class MakefileStructureViewTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testSimple() {
    val filename = "${getTestName(true)}.mk"
    myFixture.configureByFile("$testDataPath/$basePath/$filename")
    myFixture.testStructureView {
      val root = it.treeStructure.rootElement as StructureViewComponent.StructureViewTreeElementWrapper
      val file = root.value as MakefileStructureViewElement
      assertThat(file.presentation.presentableText, `is`(filename))

      assertThat(root.children, hasSize(3))
    }
  }

  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "structure"
}
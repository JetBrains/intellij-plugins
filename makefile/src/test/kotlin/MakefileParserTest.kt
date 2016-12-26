import com.intellij.testFramework.ParsingTestCase
import name.kropp.intellij.makefile.MakefileParserDefinition

class MakefileParserTest : ParsingTestCase("parser", "mk", MakefileParserDefinition()) {
  fun testHelloWorld() { doTest(true); }
  fun testVariables() { doTest(true); }
  fun testInclude() { doTest(true); }
  fun testConditionals() { doTest(true); }
  fun testConditionalsInsideRecipe() { doTest(true); }
  fun testPrerequisites() { doTest(true); }
  fun testMultipleTargets() { doTest(true); }
  fun testDefine() { doTest(true); }
  fun testEmptyRecipe() { doTest(true); }
  fun testRecipeOnTheSameLine() { doTest(true); }
  fun testDirectives() { doTest(true); }
  fun testExport() { doTest(true); }
  fun testVPath() { doTest(true); }

  override fun getTestDataPath() = "testData"
}
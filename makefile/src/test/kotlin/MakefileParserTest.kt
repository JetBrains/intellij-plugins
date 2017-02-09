import com.intellij.testFramework.ParsingTestCase
import name.kropp.intellij.makefile.MakefileParserDefinition

class MakefileParserTest : ParsingTestCase("parser", "mk", MakefileParserDefinition()) {
  fun testHelloWorld() = doTest(true)
  fun testVariables() = doTest(true)
  fun testInclude() = doTest(true)
  fun testConditionals() = doTest(true)
  fun testConditionalsInsideRecipe() = doTest(true)
  fun testConditionalVars() = doTest(true)
  fun testPrerequisites() = doTest(true)
  fun testMultipleTargets() = doTest(true)
  fun testDefine() = doTest(true)
  fun testEmptyRecipe() = doTest(true)
  fun testRecipeOnTheSameLine() = doTest(true)
  fun testDirectives() = doTest(true)
  fun testExport() = doTest(true)
  fun testVPath() = doTest(true)
  fun testComments() = doTest(true)
  fun testMultiline() = doTest(true)
  fun testTargetInsideConditional() = doTest(true)
  fun testTargetSpecificVariable() = doTest(true)
  fun testWildcard() = doTest(true)

  fun testIssue7() = doTest(true)
  fun testIssue9() = doTest(true)
  fun testIssue15() = doTest(true)

  override fun getTestDataPath() = "testData"
}
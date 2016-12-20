import com.intellij.testFramework.ParsingTestCase
import name.kropp.intellij.makefile.MakefileParserDefinition

class MakefileParserTest : ParsingTestCase("parser", "mk", MakefileParserDefinition()) {
  fun testHelloWorld() { doTest(true); }
  fun testVariables() { doTest(true); }
  fun testInclude() { doTest(true); }
  fun testConditionals() { doTest(true); }

  override fun getTestDataPath() = "testData"
}
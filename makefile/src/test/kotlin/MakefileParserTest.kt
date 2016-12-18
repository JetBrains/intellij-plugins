import com.intellij.testFramework.ParsingTestCase
import name.kropp.intellij.makefile.MakefileParserDefinition

class MakefileParserTest : ParsingTestCase("parser", "mk", MakefileParserDefinition()) {
  fun testHelloWorld() { doTest(true); }

  override fun getTestDataPath() = "testData"
}
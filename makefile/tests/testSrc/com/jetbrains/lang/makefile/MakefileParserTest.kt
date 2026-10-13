package com.jetbrains.lang.makefile

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Test

@TestApplication
class MakefileParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(
    projectFixture, "$BASE_TEST_DATA_PATH/parser", "mk",
    definitions = arrayOf(MakefileParserDefinition()),
  )

  @Test
  fun testHelloWorld() = parsing.doTest(true)
  @Test
  fun testVariables() = parsing.doTest(true)
  @Test
  fun testInclude() = parsing.doTest(true)
  @Test
  fun testConditionals() = parsing.doTest(true)
  @Test
  fun testConditionalsInsideRecipe() = parsing.doTest(true)
  @Test
  fun testConditionalVars() = parsing.doTest(true)
  @Test
  fun testConditionalAfterRecipe() = parsing.doTest(true)
  @Test
  fun testConditionalWithParens() = parsing.doTest(true)
  @Test
  fun testPrerequisites() = parsing.doTest(true)
  @Test
  fun testMultipleTargets() = parsing.doTest(true)
  @Test
  fun testDefine() = parsing.doTest(true)
  @Test
  fun testEmptyRecipe() = parsing.doTest(true)
  @Test
  fun testRecipeOnTheSameLine() = parsing.doTest(true)
  @Test
  fun testDirectives() = parsing.doTest(true)
  @Test
  fun testExport() = parsing.doTest(true)
  @Test
  fun testExportMultipleVariables() = parsing.doTest(true)
  @Test
  fun testExportMultilineVariables() = parsing.doTest(true)
  @Test
  fun testVPath() = parsing.doTest(true)
  @Test
  fun testComments() = parsing.doTest(true)
  @Test
  fun testMultiline() = parsing.doTest(true)
  @Test
  fun testTargetInsideConditional() = parsing.doTest(true)
  @Test
  fun testTargetSpecificVariable() = parsing.doTest(true)
  @Test
  fun testWildcard() = parsing.doTest(true)
  @Test
  fun testDoubleColonRule() = parsing.doTest(true)
  @Test
  fun testStaticPatternRules() = parsing.doTest(true)
  @Test
  fun testDoccomments() = parsing.doTest(true)
  @Test
  fun testFunctions() = parsing.doTest(true)
  @Test
  fun testAtSign() = parsing.doTest(true)
  @Test
  fun testElseif() = parsing.doTest(true)
  @Test
  fun testFunctionInPrerequisites() = parsing.doTest(true)
  @Test
  fun testUndefine() = parsing.doTest(true)
  @Test
  fun testMultilineComment() = parsing.doTest(true)
  @Test
  fun testSimpleVar() = parsing.doTest(true)
  @Test
  fun testWhitespace() = parsing.doTest(true)
  @Test
  fun testShellWithCurly() = parsing.doTest(true)
  @Test
  fun testNonAscii() = parsing.doTest(true)
  @Test
  fun testStrings() = parsing.doTest(true)

  @Test
  fun testIssue7() = parsing.doTest(true)
  @Test
  fun testIssue9() = parsing.doTest(true)
  @Test
  fun testIssue15() = parsing.doTest(true)
  @Test
  fun testIssue23() = parsing.doTest(true)
  @Test
  fun testIssue36() = parsing.doTest(true)
  @Test
  fun testIssue37() = parsing.doTest(true)
  @Test
  fun testIssue44() = parsing.doTest(true)
  @Test
  fun testIssue45() = parsing.doTest(true)
  @Test
  fun testIssue46() = parsing.doTest(true)
  @Test
  fun testIssue56() = parsing.doTest(true)
  @Test
  fun testIssue61() = parsing.doTest(true)
  @Test
  fun testIssue62() = parsing.doTest(true)
  @Test
  fun testIssue63() = parsing.doTest(true)
  @Test
  fun testIssue72() = parsing.doTest(true)
  @Test
  fun testIssue81() = parsing.doTest(true)
  @Test
  fun testIssue88() = parsing.doTest(true)
  @Test
  fun testIssue91() = parsing.doTest(true)
  @Test
  fun testIssue97() = parsing.doTest(true)
  @Test
  fun testIssue104() = parsing.doTest(true)
  @Test
  fun testIssue107() = parsing.doTest(true)
  @Test
  fun testIssue113() = parsing.doTest(true)
  @Test
  fun testIssue115() = parsing.doTest(true)
  @Test
  fun testIssue116() = parsing.doTest(true)
  @Test
  fun testIssue118() = parsing.doTest(true)
  @Test
  fun testIssue120() = parsing.doTest(true)
  @Test
  fun testIssue121() = parsing.doTest(true)
  @Test
  fun testIssue124() = parsing.doTest(true)
  @Test
  fun testIssue130() = parsing.doTest(true)
  @Test
  fun testIssue131() = parsing.doTest(true)
  @Test
  fun testIssue129() = parsing.doTest(true)
  @Test
  fun testIssue132() = parsing.doTest(true)
  @Test
  fun testIssue135() = parsing.doTest(true)
  @Test
  fun testIssue140() = parsing.doTest(true)
  @Test
  fun testIssue141() = parsing.doTest(true)
  @Test
  fun testIssue142() = parsing.doTest(true)
  @Test
  fun testIssue143() = parsing.doTest(true)
  @Test
  fun testIssue144() = parsing.doTest(true)
  @Test
  fun testIssue150() = parsing.doTest(true)
  @Test
  fun testIssue153() = parsing.doTest(true)
  @Test
  fun testIssue155() = parsing.doTest(true)
  @Test
  fun testIssue159() = parsing.doTest(true)
  @Test
  fun testIssue160() = parsing.doTest(true)
  @Test
  fun testIssue167() = parsing.doTest(true)
  @Test
  fun testIssue170() = parsing.doTest(true)
  @Test
  fun testIssue175() = parsing.doTest(true)
  @Test
  fun testIssue178() = parsing.doTest(true)
  @Test
  fun testIssue179() = parsing.doTest(true)
  @Test
  fun testIssue185() = parsing.doTest(true)
  @Test
  fun testIssue188() = parsing.doTest(true)
  @Test
  fun testIssue190() = parsing.doTest(true)
  @Test
  fun testIssue194() = parsing.doTest(true)

  @Test
  fun testFunctionForeach() = parsing.doTest(true)
  @Test
  fun testFunctionCall() = parsing.doTest(true)

  @Test
  fun testRecipePrefix0() = parsing.doTest(true)
  @Test
  fun testRecipePrefix1() = parsing.doTest(true)
  @Test
  fun testRecipePrefix2() = parsing.doTest(true)
  @Test
  fun testRecipePrefix3() = parsing.doTest(true)
  @Test
  fun testRecipePrefix4() = parsing.doTest(true)
  @Test
  fun testRecipePrefix5() = parsing.doTest(true)
  @Test
  fun testRecipePrefix6() = parsing.doTest(true)
  @Test
  fun testRecipePrefix7() = parsing.doTest(true)
  @Test
  fun testRecipePrefix8() = parsing.doTest(true)
  @Test
  fun testRecipePrefix9() = parsing.doTest(true)
  @Test
  fun testRecipePrefix10() = parsing.doTest(true)
  @Test
  fun testRecipePrefix11() = parsing.doTest(true)

  @Test
  fun testRecipeIndent() = parsing.doTest(true)
}
package com.intellij.terraform.template

import com.intellij.openapi.application.PathManager
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.terraform.template.psi.TftplParserDefinition
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.intellij.terraform.hil.HILParserDefinition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class TftplParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(
    projectFixture,
    PathManager.getHomePath() + "/contrib/terraform/terraform-template/test-data/parser",
    "tftpl",
    false,
    TftplParserDefinition(), HILParserDefinition(),
  )

  @BeforeEach
  fun setUp() {
    parsing.checkAllPsiRoots = false
  }

  private fun doRunTest(skipSpaces: Boolean = false, ensureNoErrorElements: Boolean = true) {
    val prevValue = parsing.skipSpaces
    parsing.skipSpaces = skipSpaces
    parsing.doTest(true, ensureNoErrorElements)
    parsing.skipSpaces = prevValue
  }

  @Test
  fun testCorrectForLoop() = doRunTest()

  @Test
  fun testNestedConstructions() = doRunTest()

  @Test
  fun testIfExpression() = doRunTest()

  @Test
  fun testForLoop() = doRunTest()

  @Test
  fun testCorrectBracesInDataLanguage() = doRunTest()

  @Test
  fun testExampleWithDataLanguage() = doRunTest()

  @Test
  fun testSimpleExamples() = doRunTest()

  @Test
  fun testForIncomplete() = doRunTest(ensureNoErrorElements = false)

  @Test
  fun testIfIncomplete() = doRunTest(ensureNoErrorElements = false)

  @Test
  fun testForLoopWithError() = doRunTest(ensureNoErrorElements = false)

  @Test
  fun testIncorrectBracesInDataLanguage() = doRunTest(ensureNoErrorElements = false)

  @Test
  fun testIncorrectOpeningBraceInIlSegment() = doRunTest(ensureNoErrorElements = false)
}
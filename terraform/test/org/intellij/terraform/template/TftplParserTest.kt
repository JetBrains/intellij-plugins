package org.intellij.terraform.template

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.ParsingTestCase
import org.intellij.terraform.hil.HILParserDefinition
import org.intellij.terraform.template.psi.TftplParserDefinition

class TftplParserTest : ParsingTestCase("", "tftpl", false, TftplParserDefinition(), HILParserDefinition()) {
  override fun getTestDataPath(): String {
    return PathManager.getHomePath() + "/contrib/terraform/test-data/terraform/template/parser"
  }

  private var shouldSkipSpaces = false

  override fun checkAllPsiRoots(): Boolean = false
  override fun skipSpaces(): Boolean = shouldSkipSpaces

  private fun doRunTest(skipSpaces: Boolean = false, ensureNoErrorElements: Boolean = true) {
    val prevValue = shouldSkipSpaces
    shouldSkipSpaces = skipSpaces
    doTest(true, ensureNoErrorElements)
    shouldSkipSpaces = prevValue
  }

  fun testCorrectForLoop() = doRunTest()
  fun testNestedConstructions() = doRunTest()
  fun testIfExpression() = doRunTest()
  fun testForLoop() = doRunTest()
  fun testCorrectBracesInDataLanguage() = doRunTest()
  fun testExampleWithDataLanguage() = doRunTest()
  fun testSimpleExamples() = doRunTest()

  fun testForIncomplete() = doRunTest(ensureNoErrorElements = false)
  fun testIfIncomplete() = doRunTest(ensureNoErrorElements = false)
  fun testForLoopWithError() = doRunTest(ensureNoErrorElements = false)
  fun testIncorrectBracesInDataLanguage() = doRunTest(ensureNoErrorElements = false)
  fun testIncorrectOpeningBraceInIlSegment() = doRunTest(ensureNoErrorElements = false)
}
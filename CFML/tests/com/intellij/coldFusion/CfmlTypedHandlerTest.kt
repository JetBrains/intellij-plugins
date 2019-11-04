package com.intellij.coldFusion

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.propertyBased.CheckHighlighterConsistency
import org.jetbrains.annotations.NonNls

class CfmlTypedHandlerTest : CfmlCodeInsightFixtureTestCase() {

  fun testSimpleTagGTCompletion() = doTest('>')

  fun testSimpleTagGTCompletion2() = doTest('>')

  fun testInnerTagGTCompletion() = doTest('>')

  fun testOuterTagGTCompletion1() = doTest('>')

  fun testOuterTagGTCompletion2() = doTest('>')

  fun testSimpleTagGTNoCompletion() = doTest('>')

  fun testInnerTagGTNoCompletion() = doTest('>')

  fun testOuterTagGTNoCompletion1() = doTest('>')

  fun testOuterTagGTNoCompletion2() = doTest('>')

  fun testSeveralTagsGTCompletion() = doTest('>')

  fun testInvokeClosingNotInsertion() = doTest('>')

  fun testModuleClosingNotInsertion() = doTest('>')

  fun testQuoteCompletion() = doTest('\"')

  fun testQuoteDeletion() = doTest('\b')

  fun testEnterHandler() = doTest('\n')

  fun testNpeEnterHandler() = doTest('\n')

  fun testEnterHandlerInsideCfFunction() = doTest('\n')

  fun testEnterHandlerAfterTemplateText() = doTest('\n')

  fun testEnterHandlerInsideHtmlBlock() = doTest('\n')

  // public void testLiveTemplate() throws Throwable { doTest('\t'); }

  // IDEA-148357, until we calculate properly where we should insert double pounds
  fun testSharpCompletion() = doTest('#')

  fun testInnerSharpCompletion() = doTest('#')

  fun testRightBracketInQuotes() = doTest(')')

  fun testNoInsertionRCurlyBracketIfIncorrect() = doTest('\n')

  fun testEditing() {
    @NonNls val s1 = "<table bgcolor=\"#FFFFFF\"><cfoutput>\n" + "  <div id=\"#bColumn2"
    val s2 = "\" />\n" + "</cfoutput></table>"
    val s = s1 + s2

    myFixture.configureByText("a.cfml", s)
    WriteCommandAction.runWriteCommandAction(project) {
      myFixture.editor.document.insertString(s1.length, "#")
    }
    CheckHighlighterConsistency.performCheck(myFixture.editor)
  }

  fun testRightBracketInsertion() = doTest('(')

  fun testRightSquareBracketInsertion() = doTest('[')

  fun testRightCurlyBracketInsertion() = doTest('{')

  fun testLeftBracketDeletion()  = doTest('\b')

  fun testLeftSquareBracketDeletion() = doTest('\b')

  fun testLeftCurlyBracketDeletion() = doTest('\b')

  /**
   * IDEA-179998 it is a regression test to reproduce wrong auto-closing tag for mixed CFML and HTML file
   */
  fun testHtmlMixedCloseTag() = doTest('/')

  fun testHtmlMixedCloseTag2() = doTest('/')

  fun testHtmlMixedCloseTag3() = doTest('/')

  //IDEA-205201
  fun testHtmlMixedCloseTag4() = doTest('/')

  //IDEA-205201
  fun testHtmlMixedCloseTag5() = doTest('/')

  fun testPureCfmlCloseTag() = doTest('/')

  fun testCfmlComplexCloseTag() = doTest('/') // IDEA-205139 regression test


  private fun doTest(typed: Char) {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)))
    myFixture.type(typed)
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)))
  }

  override fun getBasePath() = "/typedHandler"

}

package com.intellij.coldFusion

import com.intellij.coldFusion.model.files.CfmlFileType
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.testFramework.EditorTestUtil.getAllTokens
import junit.framework.TestCase
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

    val doc = DocumentImpl(s)
    val editor = EditorFactory.getInstance().createEditor(doc) as EditorEx
    try {
      var highlighter = HighlighterFactory.createHighlighter(project, CfmlFileType.INSTANCE)
      editor.highlighter = highlighter
      CommandProcessor.getInstance().executeCommand(project, {
        ApplicationManager.getApplication().runWriteAction {
          doc.insertString(s1.length, "#")
        }
      }, "", null)
      val tokensAfterUpdate = getAllTokens(highlighter)
      highlighter = HighlighterFactory.createHighlighter(project, CfmlFileType.INSTANCE)
      editor.highlighter = highlighter
      val tokensWithoutUpdate = getAllTokens(highlighter)
      TestCase.assertEquals(tokensWithoutUpdate, tokensAfterUpdate)
    }
    finally {
      EditorFactory.getInstance().releaseEditor(editor)
    }
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

  fun testPureCfmlCloseTag() = doTest('/')


  private fun doTest(typed: Char) {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)))
    myFixture.type(typed)
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)))
  }

  override fun getBasePath() = "/typedHandler"

}

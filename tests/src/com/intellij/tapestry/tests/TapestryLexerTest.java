package com.intellij.tapestry.tests;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.tapestry.psi.TmlHighlightingLexer;
import com.intellij.tapestry.psi.TmlLexer;
import com.intellij.testFramework.UsefulTestCase;

import java.io.IOException;

/**
 * @author Alexey Chmutov
 *         Date: 29.07.2009
 *         Time: 16:42:14
 */
public class TapestryLexerTest extends UsefulTestCase {

  public void testTemplateNoEL() throws Throwable {
    doTest();
  }

  public void testSingleELInAttributeValue() throws Throwable {
    doTest();
  }

  public void testELHighlightingInXmlText() throws Throwable {
    doHighlightingTest();
  }

  public void testRangeOpHighlighting() throws Throwable {
    doHighlightingTest();
  }

  public void testELHighlightingInAttr() throws Throwable {
    doHighlightingTest();
  }


  private void doTest() throws IOException {
    doTest(new TmlLexer());
  }

  private void doHighlightingTest() throws IOException {
    doTest(new TmlHighlightingLexer());
  }

  private void doTest(Lexer lexer) throws IOException {
    doTest(lexer, getTestInput(), getExpectedTextFilePath());
  }

  private String getTestInput() {
    return Util.getFileText(getDataSubpath() + getTestName(false) + Util.DOT_TML);
  }

  private String getExpectedTextFilePath() {
    return getDataSubpath() + getTestName(false) + Util.DOT_EXPECTED;
  }

  private void doTest(Lexer lexer, String testText, String expectedTextFileName) throws IOException {
    lexer.start(testText);
    String result = "";
    for (; ;) {
      IElementType tokenType = lexer.getTokenType();
      if (tokenType == null) {
        break;
      }
      String tokenText = getTokenText(lexer);
      String tokenTypeName = tokenType.toString();
      String line = tokenTypeName + " ('" + tokenText + "')\n";
      result += line;
      lexer.advance();
    }
    //if (!(new File(expectedTextFileName).exists())) {
    //  final FileWriter writer = new FileWriter(expectedTextFileName);
    //  writer.write(result);
    //  writer.close();
    //}
    assertSameLinesWithFile(expectedTextFileName, result);
  }

  private static String getTokenText(Lexer lexer) {
    return lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
  }

  protected String getDataSubpath() {
    return Util.getCommonTestDataPath() + "lexer/";
  }

}


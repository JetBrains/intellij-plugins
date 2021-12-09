package com.intellij.tapestry.tests;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.tapestry.psi.TmlHighlightingLexer;
import com.intellij.tapestry.psi.TmlLexer;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;

/**
 * @author Alexey Chmutov
 */
public class TapestryLexerTest extends UsefulTestCase {

  public void testTemplateNoEL() {
    doTest();
  }

  public void testSingleELInAttributeValue() {
    doTest();
  }

  public void testELHighlightingInXmlText() {
    doHighlightingTest();
  }

  public void testRangeOpHighlighting() {
    doHighlightingTest();
  }

  public void testELHighlightingInAttr() {
    doHighlightingTest();
  }


  private void doTest() {
    doTest(new TmlLexer());
  }

  private void doHighlightingTest() {
    doTest(new TmlHighlightingLexer());
  }

  private void doTest(Lexer lexer) {
    doTest(lexer, getTestInput(), getExpectedTextFilePath());
  }

  private String getTestInput() {
    return Util.getFileText(getDataSubpath() + getTestName(false) + Util.DOT_TML);
  }

  private String getExpectedTextFilePath() {
    return getDataSubpath() + getTestName(false) + Util.DOT_EXPECTED;
  }

  private void doTest(Lexer lexer, String testText, String expectedTextFileName) {
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


  private IdeaProjectTestFixture myFixture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // needed for various XML extension points registration
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).getFixture();
    myFixture.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }
}


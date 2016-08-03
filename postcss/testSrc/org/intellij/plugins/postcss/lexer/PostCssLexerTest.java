package org.intellij.plugins.postcss.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssTestUtils;

@TestDataPath("$CONTENT_ROOT/testData/lexer/")
public class PostCssLexerTest extends LexerTestCase {
  public void testComments() {
    doTest();
  }

  public void testAmpersand() {
    doTest();
  }

  public void testNest() {
    doTest();
  }

  public void testCustomSelector() {
    doTest();
  }

  public void testHashSignInId() {
    doTest();
  }

  public void testHashSignInPseudoFunction() {
    doTest();
  }

  private void doTest() {
    doFileTest("pcss");
  }

  @Override
  protected Lexer createLexer() {
    return new PostCssLexer();
  }

  @Override
  protected String getDirPath() {
    return PostCssTestUtils.getTestDataBasePath(getClass());
  }
}
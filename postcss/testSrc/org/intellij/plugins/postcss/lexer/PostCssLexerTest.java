package org.intellij.plugins.postcss.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.jetbrains.annotations.NotNull;

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

  public void testGreaterOrEqual() {
    doTest();
  }

  public void testLessAndLessOrEqual() {
    doTest();
  }

  public void testCustomMedia() {
    doTest();
  }

  private void doTest() {
    doFileTest("pcss");
  }

  @Override
  protected @NotNull Lexer createLexer() {
    return new PostCssLexer();
  }

  @Override
  protected @NotNull String getDirPath() {
    return PostCssTestUtils.getTestDataBasePath(getClass());
  }
}
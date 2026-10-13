package org.intellij.plugins.postcss.lexer;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

@TestFixtures
@TestDataPath("$CONTENT_ROOT/../testData/lexer/")
public class PostCssLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(PostCssTestUtils.getFullTestDataPath(PostCssLexerTest.class), () -> new PostCssLexer());

  @Test
  public void testComments() {
    doTest();
  }

  @Test
  public void testAmpersand() {
    doTest();
  }

  @Test
  public void testNest() {
    doTest();
  }

  @Test
  public void testCustomSelector() {
    doTest();
  }

  @Test
  public void testHashSignInId() {
    doTest();
  }

  @Test
  public void testHashSignInPseudoFunction() {
    doTest();
  }

  @Test
  public void testGreaterOrEqual() {
    doTest();
  }

  @Test
  public void testLessAndLessOrEqual() {
    doTest();
  }

  @Test
  public void testCustomMedia() {
    doTest();
  }

  private void doTest() {
    lexer.get().doFileTest("pcss");
  }
}

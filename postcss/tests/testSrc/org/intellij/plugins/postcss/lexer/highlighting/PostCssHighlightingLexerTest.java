package org.intellij.plugins.postcss.lexer.highlighting;

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixture;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixtures;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.intellij.plugins.postcss.lexer.PostCssHighlightingLexer;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.LexerTestFixtureKt.lexerFixture;

@TestFixtures
@TestDataPath("$CONTENT_ROOT/../testData/lexer/highlighting/")
public class PostCssHighlightingLexerTest {
  private final TestFixture<LexerTestFixture> lexer =
    lexerFixture(PostCssTestUtils.getFullTestDataPath(PostCssHighlightingLexerTest.class), () -> new PostCssHighlightingLexer());

  @Test
  public void testNestedRules() {
    doTest();
  }

  @Test
  public void testMultiNested() {
    doTest();
  }

  @Test
  public void testAttributeSelectorInNestedRuleset() {
    doTest();
  }

  @Test
  public void testKeyframes() {
    doTest();
  }

  @Test
  public void testPropertyAfterKeyframes() {
    doTest();
  }

  @Test
  public void testPropertyNames() {
    doTest();
  }

  @Test
  public void testPseudoSelectors() {
    doTest();
  }

  @Test
  public void testSelectorSuffix() {
    doTest();
  }

  @Test
  public void testViewport() {
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
  public void testMediaRangeInverted() {
    doTest();
  }

  @Test
  public void testCustomMedia() {
    doTest();
  }

  @Test
  public void testUnits() {
    doTest();
  }

  private void doTest() {
    lexer.get().doFileTest("pcss");
  }
}

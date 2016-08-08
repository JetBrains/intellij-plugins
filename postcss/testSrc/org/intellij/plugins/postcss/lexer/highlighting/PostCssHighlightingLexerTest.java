package org.intellij.plugins.postcss.lexer.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.psi.css.impl.util.CssHighlighterLexer;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.intellij.plugins.postcss.lexer.PostCssHighlightingLexer;

import java.util.Collections;
import java.util.Set;

@TestDataPath("$CONTENT_ROOT/testData/lexer/highlighting/")
public class PostCssHighlightingLexerTest extends LexerTestCase {
  private static final Set<String> DEFAULT_PROPERTY_VALUES =
    ContainerUtil.union(CssHighlighterLexer.Lazy.DEFAULT_PROPERTY_VALUES, Collections.singleton("all"));

  public void testNestedRules() {
    doTest();
  }

  public void testMultiNested() {
    doTest();
  }

  public void testAttributeSelectorInNestedRuleset() {
    doTest();
  }

  public void testKeyframes() {
    doTest();
  }

  public void testPropertyAfterKeyframes() {
    doTest();
  }

  public void testPropertyNames() {
    doTest();
  }

  public void testPseudoSelectors() {
    doTest();
  }

  public void testSelectorSuffix() {
    doTest();
  }

  public void testTagName() {
    doTest();
  }

  public void testViewport() {
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

  public void testMediaRangeInverted() {
    doTest();
  }

  public void testCustomMedia() {
    doTest();
  }

  private void doTest() {
    doFileTest("pcss");
  }

  @Override
  protected Lexer createLexer() {
    return new PostCssHighlightingLexer(DEFAULT_PROPERTY_VALUES);
  }

  @Override
  protected String getDirPath() {
    return PostCssTestUtils.getTestDataBasePath(getClass());
  }
}
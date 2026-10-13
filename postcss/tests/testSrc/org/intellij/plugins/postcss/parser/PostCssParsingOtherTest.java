package org.intellij.plugins.postcss.parser;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class PostCssParsingOtherTest extends PostCssParsingTest {
  public PostCssParsingOtherTest() {
    super("other");
  }

  @Test
  public void testSimpleVars() {
    doTest();
  }

  @Test
  public void testApplyAtRule() {
    doTest();
  }

  @Test
  public void testScopeAtRule() {
    doTest();
  }

  @Test
  public void testStartingStyleAtRule() { doTest(); }

  @Test
  public void testIfFunction() {
    doTest();
  }
}
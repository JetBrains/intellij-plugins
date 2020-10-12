package org.intellij.plugins.postcss.parser;

public class PostCssParsingOtherTest extends PostCssParsingTest {
  public PostCssParsingOtherTest() {
    super("other");
  }

  public void testSimpleVars() {
    doTest();
  }

  public void testApplyAtRule() {
    doTest();
  }
}
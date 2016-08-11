package org.intellij.plugins.postcss.parser;

public class PostCssParsingApplyTest extends PostCssParsingTest {
  public PostCssParsingApplyTest() {
    super("apply");
  }

  public void testApplyInsideAtRule() {
    doTest();
  }
  public void testApplyInsideRuleset() {
    doTest();
  }
  public void testApplyTopLevel() {
    doTest();
  }
  public void testApplyWithoutIdent() {
    doTest();
  }
  public void testApplyWithoutIdentAndSemicolon() {
    doTest();
  }
  public void testApplyWithoutSemicolon() {
    doTest();
  }
  public void testApplyWithoutSemicolonBeforeBrace() {
    doTest();
  }
  public void testApplyWithParen() {
    doTest();
  }
}
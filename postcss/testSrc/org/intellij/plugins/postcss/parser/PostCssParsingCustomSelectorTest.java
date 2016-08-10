package org.intellij.plugins.postcss.parser;

public class PostCssParsingCustomSelectorTest extends PostCssParsingTest {
  public PostCssParsingCustomSelectorTest() {
    super("customSelector");
  }

  public void testCustomSelector() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithoutSemicolon() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithoutColon() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithPseudoClasses() {
    doTest();
  }
  public void testCustomSelectorUsageWithPseudoClasses() {
    doTest();
  }
  public void testCustomSelectorInsideRuleset() {
    doTest();
  }
  public void testCustomSelectorInsideAtRule() {
    doTest();
  }
  public void testCustomSelectorWithWhitespace() {
    doTest();
  }
  public void testCustomSelectorWithoutTwoDashes() {
    doTest();
  }
}
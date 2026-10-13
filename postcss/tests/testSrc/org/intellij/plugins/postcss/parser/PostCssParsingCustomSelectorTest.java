package org.intellij.plugins.postcss.parser;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class PostCssParsingCustomSelectorTest extends PostCssParsingTest {
  public PostCssParsingCustomSelectorTest() {
    super("customSelector");
  }

  @Test
  public void testCustomSelector() {
    doTest();
  }
  @Test
  public void testCustomSelectorDefinitionWithoutSemicolon() {
    doTest();
  }
  @Test
  public void testCustomSelectorWithoutSemicolonBeforeBrace() {
    doTest();
  }
  @Test
  public void testCustomSelectorDefinitionWithoutColon() {
    doTest();
  }
  @Test
  public void testCustomSelectorDefinitionWithPseudoClasses() {
    doTest();
  }
  @Test
  public void testCustomSelectorUsageWithPseudoClasses() {
    doTest();
  }
  @Test
  public void testCustomSelectorInsideRuleset() {
    doTest();
  }
  @Test
  public void testCustomSelectorInsideAtRule() {
    doTest();
  }
  @Test
  public void testCustomSelectorWithWhitespace() {
    doTest();
  }
  @Test
  public void testCustomSelectorWithoutTwoDashes() {
    doTest();
  }
}
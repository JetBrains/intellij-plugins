package org.intellij.plugins.postcss.completion;

import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorCompletionTest extends PostCssFixtureTestCase {

  public void testCustomSelectorTopLevel() {
    doTest();
  }

  public void testCustomSelectorInsideRuleset() {
    doTest();
  }

  public void testCustomSelectorInsideAtRule() {
    doTest();
  }

  public void testCustomSelectorInsideNest() {
    doTest();
  }

  public void testSpaceAfterCaret() {
    doTest();
  }

  public void testSemicolonAfterCaret() {
    doTest();
  }

  public void testSpaceAndSemicolonAfterCaret() {
    doTest();
  }

  public void testSemicolonWithWhitespacesAfterCaret() {
    doTest();
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelector";
  }

  private void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }

}

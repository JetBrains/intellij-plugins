package org.intellij.plugins.postcss.completion;

import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaCompletionTest extends PostCssFixtureTestCase {

  public void testCustomMediaTopLevel() {
    doTest();
  }

  public void testCustomMediaInsideRuleset() {
    doTest();
  }

  public void testCustomMediaInsideAtRule() {
    doTest();
  }

  public void testCustomMediaInsideNest() {
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

  private void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}
package org.intellij.plugins.postcss.completion;

import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssKeywordCompletionTest extends PostCssFixtureTestCase {

  public void testNestInsideRuleset() {
    doTest();
  }

  public void testNestInsideSelector() {
    doTest();
  }

  public void testNestTopLevel() {
    doTest();
  }

  public void testNestInsideTopLevelAtRule() {
    doTest();
  }

  public void testNestInsideAtRule() {
    doTest();
  }

  public void testNestInsideNest() {
    doTest();
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nesting";
  }

  private void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }

}

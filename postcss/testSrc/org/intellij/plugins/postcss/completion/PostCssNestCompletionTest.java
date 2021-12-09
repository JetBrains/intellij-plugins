package org.intellij.plugins.postcss.completion;

import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssNestCompletionTest extends PostCssFixtureTestCase {

  public void testNestInsideSelector() {
    doTest();
  }

  public void testNestInsideSelectorList() {
    doTest();
  }

  public void testNestTopLevel() {
    doTest();
  }

  public void testNestInsideTopLevelAtRule() {
    doTest();
  }

  public void testNestInsidePageAtRule() {
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

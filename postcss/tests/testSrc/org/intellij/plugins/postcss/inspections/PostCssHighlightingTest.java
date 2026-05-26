package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssHighlightingTest extends PostCssQuickFixTest {

  public void testKeywords() {
    doTest();
  }

  public void testTags() {
    doTest();
  }

  public void testAttributeName() {
    doTest();
  }

  public void testClassName() {
    doTest();
  }

  private void doTest() {
    myFixture.testHighlighting(true, true, true, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "highlighting";
  }
}
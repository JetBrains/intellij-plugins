package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssHighlightingTest extends PostCssQuickFixTest {

  public void testKeywords() throws Throwable {
    doTest();
  }

  public void testTags() throws Throwable {
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
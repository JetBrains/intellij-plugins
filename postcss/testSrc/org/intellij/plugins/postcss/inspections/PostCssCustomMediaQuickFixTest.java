package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomMediaInspection.class);
  }

  public void testAddCustomMediaPrefixEmpty() {
    doTestAddDashes();
  }

  public void testAddCustomMediaPrefixDash() {
    doTestAddDashes();
  }

  private void doTestAddDashes() {
    doTest("Add '--' to custom media");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}
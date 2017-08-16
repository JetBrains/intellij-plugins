package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomSelectorInspection.class);
  }

  public void testAddCustomSelectorPrefixEmpty() {
    doTestAddDashes();
  }

  public void testAddCustomSelectorPrefixColon() {
    doTestAddDashes();
  }

  public void testAddCustomSelectorPrefixColonDash() {
    doTestAddDashes();
  }

  private void doTestAddDashes() {
    doTest("Add ':--' to custom selector");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelectors";
  }
}
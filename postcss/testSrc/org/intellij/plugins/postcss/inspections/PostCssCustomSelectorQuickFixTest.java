package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomSelectorInspection.class);
  }

  public void testAddCustomSelectorPrefixEmpty() throws Throwable {
    doTestAddDashes();
  }

  public void testAddCustomSelectorPrefixColon() throws Throwable {
    doTestAddDashes();
  }

  public void testAddCustomSelectorPrefixColonDash() throws Throwable {
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
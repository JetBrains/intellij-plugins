package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesSetQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomPropertiesSetInspection.class);
  }

  public void testAddPrefixEmpty() throws Throwable {
    doTestAddDashes();
  }

  public void testAddPrefixDash() throws Throwable {
    doTestAddDashes();
  }

  public void testAddPrefixBeforeBrace() throws Throwable {
    doTestAddDashes();
    myFixture.testHighlighting(false, false, false, myFixture.getFile().getVirtualFile());
  }

  public void testWrapWithRootRule() throws Throwable {
    doTest("Wrap custom properties set with `:root` rule");
  }

  private void doTestAddDashes() {
    doTest("Add '--' to custom properties set name");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customPropertiesSet";
  }
}
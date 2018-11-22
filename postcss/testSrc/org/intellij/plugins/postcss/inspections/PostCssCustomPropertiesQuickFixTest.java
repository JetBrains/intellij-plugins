package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomPropertiesInspection.class);
  }

  public void testWrapWithRootRule() {
    doTestWrapWithRoot();
  }

  public void testWrapWithRootRuleSemicolon() {
    doTestWrapWithRoot();
  }

  private void doTestWrapWithRoot() {
    doTest("Wrap with `:root` rule");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customPropertiesSet";
  }
}
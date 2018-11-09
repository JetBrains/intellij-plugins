package org.intellij.plugins.postcss.inspections;

import com.intellij.psi.css.inspections.invalid.CssUnresolvedCustomPropertySetInspection;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomPropertiesInspection.class, CssUnresolvedCustomPropertySetInspection.class);
  }

  public void testAddPrefixEmpty() {
    doTestAddDashes();
  }

  public void testAddPrefixDash() {
    doTestAddDashes();
  }

  public void testAddPrefixBeforeBrace() {
    doTestAddDashes();
    myFixture.testHighlighting(false, false, false, myFixture.getFile().getVirtualFile());
  }

  public void testWrapWithRootRule() {
    doTestWrapWithRoot();
  }

  public void testWrapWithRootRuleSemicolon() {
    doTestWrapWithRoot();
  }

  private void doTestAddDashes() {
    doTest("Add '--' to custom property set name");
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
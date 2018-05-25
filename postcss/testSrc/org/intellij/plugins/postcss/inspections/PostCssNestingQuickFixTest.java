package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssNestingQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssNestingInspection.class);
  }

  public void testDeleteAmpersand() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromClass() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromId() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromSimpleSelector() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromPseudoClass() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromPseudoFunction() {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromAttribute() {
    doTestDeleteAmpersand();
  }

  public void testDeleteNest() {
    doTest("Delete '@nest'");
  }

  public void testAddAmpersand() {
    doTest("Add '&' to selector");
  }

  public void testAddNest() {
    doTest("Add '@nest' to selector");
  }

  private void doTestDeleteAmpersand(){
    doTest("Delete '&'");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nesting";
  }
}
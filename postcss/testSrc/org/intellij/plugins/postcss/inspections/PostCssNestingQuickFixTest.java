package org.intellij.plugins.postcss.inspections;

import org.jetbrains.annotations.NotNull;

public class PostCssNestingQuickFixTest extends PostCssQuickFixTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssNestingInspection.class);
  }

  public void testDeleteAmpersand() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromClass() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromId() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromSimpleSelector() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromPseudoClass() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromPseudoFunction() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteAmpersandFromAttribute() throws Throwable {
    doTestDeleteAmpersand();
  }

  public void testDeleteNest() throws Throwable {
    doTest("Delete '@nest'");
  }

  public void testAddAmpersand() throws Throwable {
    doTest("Add '&' to selector");
  }

  public void testAddNest() throws Throwable {
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
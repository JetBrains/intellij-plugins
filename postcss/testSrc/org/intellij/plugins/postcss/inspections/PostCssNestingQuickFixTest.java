package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssNestingQuickFixTest extends PostCssFixtureTestCase {
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
    doInspectionTest(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss", "Delete '&'");
  }

  private void doTest(String message){
    doInspectionTest(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss", message);
  }

  protected void doInspectionTest(final String testFile,
                                  final String resultFile,
                                  final String quickFixName) {
    myFixture.configureByFile(testFile);
    final IntentionAction intentionAction = myFixture.findSingleIntention(quickFixName);
    myFixture.launchAction(intentionAction);
    myFixture.checkResultByFile(resultFile);
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nesting";
  }
}
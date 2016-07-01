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
    doInspectionTest("deleteAmpersand.pcss", "deleteAmpersand_after.pcss", "Delete '&'");
  }

  public void testDeleteNest() throws Throwable {
    doInspectionTest("deleteNest.pcss", "deleteNest_after.pcss", "Delete '@nest'");
  }

  public void testAddAmpersand() throws Throwable {
    doInspectionTest("addAmpersand.pcss", "addAmpersand_after.pcss", "Add '&' to selector");
  }

  public void testAddNest() throws Throwable {
    doInspectionTest("addNest.pcss", "addNest_after.pcss", "Add '@nest' to selector");
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
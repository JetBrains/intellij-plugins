package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorQuickFixTest extends PostCssFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomSelectorInspection.class);
  }

  public void testAddCustomSelectorPrefixEmpty() throws Throwable {
    doInspectionTest("addCustomSelectorPrefixEmpty.pcss", "addCustomSelectorPrefixEmpty_after.pcss", "Add ':--' to custom selector");
  }

  public void testAddCustomSelectorPrefixColon() throws Throwable {
    doInspectionTest("addCustomSelectorPrefixColon.pcss", "addCustomSelectorPrefixColon_after.pcss", "Add ':--' to custom selector");
  }

  public void testAddCustomSelectorPrefixColonDash() throws Throwable {
    doInspectionTest("addCustomSelectorPrefixColonDash.pcss", "addCustomSelectorPrefixColonDash_after.pcss", "Add ':--' to custom selector");
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
    return "customSelectors";
  }
}
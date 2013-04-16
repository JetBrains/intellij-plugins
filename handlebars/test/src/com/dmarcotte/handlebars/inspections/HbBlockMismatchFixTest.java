package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class HbBlockMismatchFixTest extends LightPlatformCodeInsightFixtureTestCase {
  public HbBlockMismatchFixTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testWrongCloseBlock1() {
    doTest("Change block end");
  }

  public void testWrongCloseBlock2() {
    doTest("Change block end");
  }

  public void testWrongOpenBlock1() {
    doTest("Change block start");
  }

  public void testWrongOpenBlock2() {
    doTest("Change block start");
  }

  private void doTest(String intentionHint) {
    myFixture.configureByFile("inspections/before" + getTestName(false) + ".hbs");
    final IntentionAction intention = myFixture.findSingleIntention(intentionHint);
    new WriteCommandAction.Simple(myFixture.getProject()) {
      @Override
      protected void run() throws Throwable {
        myFixture.launchAction(intention);
      }
    }.execute();
    myFixture.checkResultByFile("inspections/after" + getTestName(false) + ".hbs");
  }


  @Override
  protected String getBasePath() {
    return "/inspections";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH;
  }
}

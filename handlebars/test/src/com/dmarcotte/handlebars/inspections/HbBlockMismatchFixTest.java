package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class HbBlockMismatchFixTest extends LightPlatformCodeInsightFixtureTestCase {

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

  public void testWrongOpenRawBlock() {
    doTest("Change block start");
  }

  private void doTest(String intentionHint) {
    myFixture.configureByFile("inspections/before" + getTestName(false) + ".hbs");
    myFixture.launchAction(myFixture.findSingleIntention(intentionHint));
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

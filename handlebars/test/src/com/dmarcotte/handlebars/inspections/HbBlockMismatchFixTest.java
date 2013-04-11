package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class HbBlockMismatchFixTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testWrongCloseBlock1() {
    doTest();
  }

  public void testWrongCloseBlock2() {
    doTest();
  }

  public void testWrongOpenBlock1() {
    doTest();
  }

  public void testWrongOpenBlock2() {
    doTest();
  }

  private void doTest() {
    myFixture.configureByFile("inspections/before" + getTestName(false) + ".hbs");
    // TODO
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

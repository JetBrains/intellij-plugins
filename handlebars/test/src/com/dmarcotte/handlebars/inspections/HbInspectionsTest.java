package com.dmarcotte.handlebars.inspections;


import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class HbInspectionsTest extends LightPlatformCodeInsightFixtureTestCase {

  public void testEmptyBlock() {
    doTest();
  }

  private void doTest() {
    myFixture.testInspection(getTestName(false), new LocalInspectionToolWrapper(new HbEmptyBlockInspection()));
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + "/inspections";
  }
}

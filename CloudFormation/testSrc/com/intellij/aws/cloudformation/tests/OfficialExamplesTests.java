package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection;
import com.intellij.testFramework.InspectionTestCase;

public class OfficialExamplesTests extends InspectionTestCase {
  public void testFormatViolationInspection() {
    final FormatViolationInspection inspection = new FormatViolationInspection();
    doTest(".", inspection);
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("/officialExamples/");
  }
}

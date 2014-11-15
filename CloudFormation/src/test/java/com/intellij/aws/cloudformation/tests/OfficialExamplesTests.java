package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection;
import com.intellij.testFramework.InspectionFixtureTestCase;

public class OfficialExamplesTests extends InspectionFixtureTestCase {
  public void testFormatViolationInspection() throws Exception {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("officialExamples"), new FormatViolationInspection());
  }
}

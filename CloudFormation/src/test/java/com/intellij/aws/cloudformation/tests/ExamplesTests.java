package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection;
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection;
import com.intellij.testFramework.InspectionFixtureTestCase;
import com.intellij.testFramework.InspectionTestCase;

public class ExamplesTests extends InspectionFixtureTestCase {
  public void testFormatViolationInspection() throws Exception {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), new FormatViolationInspection());
  }

  public void testUnresolvedReferences() throws Exception {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), new UnresolvedReferencesInspection());
  }
}

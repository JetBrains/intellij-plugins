package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection
import com.intellij.testFramework.InspectionFixtureTestCase

class InspectionsTests : InspectionFixtureTestCase() {
  fun testFormatViolationInspection() {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("inspections"), FormatViolationInspection())
  }
}

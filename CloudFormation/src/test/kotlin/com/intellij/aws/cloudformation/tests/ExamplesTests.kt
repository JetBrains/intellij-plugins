package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection
import com.intellij.testFramework.InspectionFixtureTestCase

class ExamplesTests : InspectionFixtureTestCase() {
  fun testFormatViolationInspection() {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), FormatViolationInspection())
  }

  fun testUnresolvedReferences() {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), UnresolvedReferencesInspection())
  }
}

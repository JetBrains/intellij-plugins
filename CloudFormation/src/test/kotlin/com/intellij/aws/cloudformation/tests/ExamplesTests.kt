package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection
import com.intellij.testFramework.InspectionFixtureTestCase

class ExamplesTests : InspectionFixtureTestCase() {
  @Throws(Exception::class)
  fun testFormatViolationInspection() {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), FormatViolationInspection())
  }

  @Throws(Exception::class)
  fun testUnresolvedReferences() {
    doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples"), UnresolvedReferencesInspection())
  }
}

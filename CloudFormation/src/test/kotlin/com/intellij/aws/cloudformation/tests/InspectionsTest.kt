package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.JsonFormatViolationInspection
import com.intellij.testFramework.InspectionFixtureTestCase

class InspectionsTest : InspectionFixtureTestCase() {
  fun testFormatViolationInspection() {
    myFixture.testDataPath = TestUtil.testDataRoot.path
    doTest("inspections", JsonFormatViolationInspection())
  }
}

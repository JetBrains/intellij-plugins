package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.JsonFormatViolationInspection
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.testFramework.InspectionFixtureTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Test

@TestApplication
class InspectionsTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  @Test
  fun testFormatViolationInspection() {
    myFixture.testDataPath = TestUtil.testDataRoot.path
    InspectionFixtureTestUtil.doTest(myFixture, "inspections", JsonFormatViolationInspection())
  }
}

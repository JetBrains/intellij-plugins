package com.intellij.aws.cloudformation.tests

import com.google.common.io.Files
import com.intellij.aws.cloudformation.inspections.FormatViolationInspection
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.InspectionFixtureTestCase

import java.io.File

class OfficialExamplesTests : InspectionFixtureTestCase() {
  @Throws(Exception::class)
  fun testFormatViolationInspection() {
    runInspection("format", FormatViolationInspection())
  }

  @Throws(Exception::class)
  fun testUnresolvedReferencesInspection() {
    runInspection("resolve", UnresolvedReferencesInspection())
  }

  @Throws(Exception::class)
  private fun runInspection(inspectionMoniker: String, inspectionTool: LocalInspectionTool) {
    val examplesFolder = TestUtil.getTestDataFile(EXAMPLES_FOLDER_NAME)

    val expectedFile = File(examplesFolder, "expected.xml")
    val expectedFilePerInspection = File(examplesFolder, "expected-$inspectionMoniker.xml")

    Files.copy(expectedFilePerInspection, expectedFile)

    try {
      doTest(TestUtil.getTestDataPathRelativeToIdeaHome(EXAMPLES_FOLDER_NAME), inspectionTool)
    } finally {
      expectedFile.delete()
    }
  }

  companion object {
    private val EXAMPLES_FOLDER_NAME = "officialExamples"
  }
}

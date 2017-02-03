package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.FormatViolationInspection
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.InspectionFixtureTestCase
import java.io.File

class ExamplesTests : InspectionFixtureTestCase() {
  private fun runTestOnCopy(name: String, tool: LocalInspectionTool) {
    val examplesSrc = TestUtil.getTestDataFile("examples/src")
    val nameDir = TestUtil.getTestDataFile("examples/$name")
    val nameSrc = File(nameDir, "src")

    FileUtil.copyDir(examplesSrc, nameSrc)
    try {
      doTest(TestUtil.getTestDataPathRelativeToIdeaHome("examples/$name"), tool)
    } finally {
      FileUtil.delete(nameSrc)
    }
  }

  fun testFormatViolationInspection() = runTestOnCopy("inspections", FormatViolationInspection())
  fun testUnresolvedReferences() = runTestOnCopy("unresolved", UnresolvedReferencesInspection())
}

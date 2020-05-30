package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.JsonFormatViolationInspection
import com.intellij.aws.cloudformation.inspections.JsonUnresolvedReferencesInspection
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.InspectionFixtureTestCase
import java.io.File

class ExamplesTests : InspectionFixtureTestCase() {
  private fun runTestOnCopy(name: String, tool: LocalInspectionTool) {
    myFixture.testDataPath = TestUtil.testDataRoot.path

    val examplesSrc = TestUtil.getTestDataFile("examples/src")
    val nameDir = TestUtil.getTestDataFile("examples/$name")
    val nameSrc = File(nameDir, "src")

    FileUtil.copyDir(examplesSrc, nameSrc)
    try {
      doTest("examples/$name", tool)
    } finally {
      FileUtil.delete(nameSrc)
    }
  }

  fun testFormatViolationInspection() = runTestOnCopy("inspections", JsonFormatViolationInspection())
  fun testUnresolvedReferences() = runTestOnCopy("unresolved", JsonUnresolvedReferencesInspection())
}

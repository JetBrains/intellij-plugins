package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.UsefulTestCase
import java.io.File

class CloudFormationInspectionsTests : LightPlatformCodeInsightTestCase() {
  fun testNoOutputs() = inspectionsTest("noOutputs.template")
  fun testMaxOutputs() = inspectionsTest("maxOutputs.template")
  fun testNoResourcesSection() = inspectionsTest("noResourcesSection.template")
  fun testNoResources() = inspectionsTest("noResources.template")

  fun inspectionsTest(fileName: String) {
    configureByFile(fileName)

    val parsed = CloudFormationParser.parse(myFile)
    UsefulTestCase.assertEmpty(parsed.problems)

    val actualProblems = CloudFormationInspections.inspectFile(parsed)
    val actualProblemsString = TestUtil.renderProblems(myFile, actualProblems.problems)

    TestUtil.checkContent(File(testDataPath, fileName.removeSuffix(".template") + ".expected"), actualProblemsString)
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("inspections")
  }
}

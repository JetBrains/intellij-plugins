package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.UsefulTestCase
import java.io.File

class CloudFormationInspectionsTest : LightPlatformCodeInsightTestCase() {
  fun testNoOutputs() = inspectionsTest("noOutputs.template")
  fun testMaxOutputs() {
    CloudFormationInspections.customMaxMetadataOutputs = 60
    try {
      inspectionsTest("maxOutputs.template")
    } finally {
      CloudFormationInspections.customMaxMetadataOutputs = -1
    }
  }

  fun testNoResourcesSection() = inspectionsTest("noResourcesSection.template")
  fun testNoResources() = inspectionsTest("noResources.template")

  fun inspectionsTest(fileName: String) {
    configureByFile(fileName)

    val parsed = CloudFormationParser.parse(file)
    UsefulTestCase.assertEmpty(parsed.problems)

    val actualProblems = CloudFormationInspections.inspectFile(parsed)
    val actualProblemsString = TestUtil.renderProblems(file, actualProblems.problems)

    TestUtil.checkContent(File(testDataPath, fileName.removeSuffix(".template") + ".expected"), actualProblemsString)
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("inspections")
  }
}

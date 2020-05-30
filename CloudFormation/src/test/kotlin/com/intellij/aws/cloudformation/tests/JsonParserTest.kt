package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import java.io.File

class JsonParserTest : LightPlatformCodeInsightTestCase() {
  fun testFile1() = runTest("file1")
  fun testWrongResources() = runTest("wrongResources")

  private fun runTest(name: String) {
    configureByFile("$name.template")
    val parsed = CloudFormationParser.parse(file)
    TestUtil.checkContent(
        File(testDataPath, "$name.expected"),
        TestUtil.renderProblems(file, parsed.problems)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("parser/json")
  }
}

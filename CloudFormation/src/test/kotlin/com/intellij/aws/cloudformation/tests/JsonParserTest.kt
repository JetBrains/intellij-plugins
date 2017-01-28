package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import java.io.File

class JsonParserTest : LightPlatformCodeInsightTestCase() {
  fun testFile1() = runTest("file1")
  fun testWrongResources() = runTest("wrongResources")

  fun runTest(name: String) {
    configureByFile("$name.template")
    val parsed = CloudFormationParser.parse(myFile)
    TestUtil.checkContent(
        File(testDataPath, "$name.expected"),
        TestUtil.renderProblems(myFile, parsed.problems)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("parser/json")
  }
}

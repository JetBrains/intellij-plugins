package com.intellij.aws.cloudformation.tests

import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import java.io.File

class ReferencesTest: LightPlatformCodeInsightTestCase() {
  fun testRefRange() {
    configureByFile("refRange.yaml")
    TestUtil.checkContent(
        File(testDataPath, "refRange.yaml.expected"),
        TestUtil.renderReferences(file)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("references")
  }
}

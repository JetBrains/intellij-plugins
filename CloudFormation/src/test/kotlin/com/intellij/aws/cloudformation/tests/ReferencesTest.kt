package com.intellij.aws.cloudformation.tests

import com.intellij.testFramework.LightJavaCodeInsightTestCase
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import java.io.File

class ReferencesTest: LightJavaCodeInsightTestCase() {
  fun testRefRange() {
    configureByFile("refRange.yaml")
    TestUtil.checkContent(
        File(testDataPath, "refRange.yaml.expected"),
        TestUtil.renderReferences(LightPlatformCodeInsightTestCase.myFile)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("references")
  }
}

package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.inspections.JsonFormatViolationInspection
import com.intellij.aws.cloudformation.inspections.JsonUnresolvedReferencesInspection
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.testFramework.InspectionFixtureTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Test
import java.io.File

@TestApplication
class ExamplesTests {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  private fun runTestOnCopy(name: String, tool: LocalInspectionTool) {
    myFixture.testDataPath = TestUtil.testDataRoot.path

    val examplesSrc = TestUtil.getTestDataFile("examples/src")
    val nameDir = TestUtil.getTestDataFile("examples/$name")
    val nameSrc = File(nameDir, "src")

    FileUtil.copyDir(examplesSrc, nameSrc)
    try {
      InspectionFixtureTestUtil.doTest(myFixture, "examples/$name", tool)
    } finally {
      FileUtil.delete(nameSrc)
    }
  }

  @Test
  fun testFormatViolationInspection(): Unit = runTestOnCopy("inspections", JsonFormatViolationInspection())
  @Test
  fun testUnresolvedReferences(): Unit = runTestOnCopy("unresolved", JsonUnresolvedReferencesInspection())
}

package com.jetbrains.lang.makefile

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.lang.makefile.inspections.MakefileUnresolvedPrerequisiteInspection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/makefile/tests/testData")
class MakefileInspectionsTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private val basePath =
    "inspections"

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    myFixture.enableInspections(MakefileUnresolvedPrerequisiteInspection::class.java)
  }

  private fun doTest() {
    myFixture.testHighlighting(true, false, true, "$basePath/$testName.mk")
  }

  @Test
  fun unresolved(): Unit = doTest()

  @Test
  fun multiunresolved(): Unit = doTest()

  @Test
  fun unresolvedWithPattern(): Unit = doTest()
}
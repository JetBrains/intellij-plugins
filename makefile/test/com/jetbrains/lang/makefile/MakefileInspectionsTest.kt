package com.jetbrains.lang.makefile

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.lang.makefile.inspections.MakefileUnresolvedPrerequisiteInspection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MakefileInspectionsTest : BasePlatformTestCase() {
  override fun getTestDataPath() =
    BASE_TEST_DATA_PATH

  override fun getBasePath() =
    "inspections"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(MakefileUnresolvedPrerequisiteInspection::class.java)
  }

  private fun doTest() {
    myFixture.testHighlighting(true, false, true, "$basePath/${getTestName(true)}.mk")
  }

  @Test
  fun unresolved() = doTest()

  @Test
  fun multiunresolved() = doTest()

  @Test
  fun unresolvedWithPattern() = doTest()
}
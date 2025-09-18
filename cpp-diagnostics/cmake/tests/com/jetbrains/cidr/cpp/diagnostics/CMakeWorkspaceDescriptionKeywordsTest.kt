package com.jetbrains.cidr.cpp.diagnostics

import com.jetbrains.cidr.cpp.CPPTestCase
import org.junit.Assert.assertTrue

class CMakeWorkspaceDescriptionKeywordsTest : CPPTestCase() {

  @Throws(Exception::class)
  override fun setUpFixtures() {
    super.setUpFixtures()
    myProjectFixture.initProject("simple-cmake-project")
    myProjectFixture.waitForReloads()
  }

  fun testCMakeWorkspaceDescriptionKeywords() {
    val out = collectCidrWorkspaces(project).toText()

    // CMake-specific provider should contribute these keywords
    assertOutputContains(out, "Auto reload enabled:")
    assertOutputContains(out, "Profile:")
    assertOutputContains(out, "buildType:")
    assertOutputContains(out, "effective toolchain:")
    assertOutputContains(out, "effective generation dir:")
  }

  private fun assertOutputContains(text: String, needle: String) {
    assertTrue("Expected to find '$needle' in output.\nOutput:\n$text", text.contains(needle))
  }
}

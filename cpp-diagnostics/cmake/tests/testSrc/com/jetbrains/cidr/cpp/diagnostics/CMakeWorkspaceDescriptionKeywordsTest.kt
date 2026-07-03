package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class CMakeWorkspaceDescriptionKeywordsTest {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()
  }

  private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
  private val project by cmakeProjectTestFixture(tempDir)

  @Test
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
    assertTrue(text.contains(needle), "Expected to find '$needle' in output.\nOutput:\n$text")
  }
}

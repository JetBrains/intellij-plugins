package com.intellij.clion.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.cpp.diagnostics.collectCidrWorkspaces
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class CMakeWorkspaceDescriptionKeywordsTest {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()

    private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
    private val project by cmakeProjectTestFixture(tempDir)
  }

  @Test
  fun testCMakeWorkspaceDescriptionKeywords() {
    val out = collectCidrWorkspaces(project).toText()

    // CMake-specific provider should contribute these keywords
    assertThat(out).contains(
      "Auto reload enabled:",
      "Profile:",
      "buildType:",
      "effective toolchain:",
      "effective generation dir:"
    )
  }
}

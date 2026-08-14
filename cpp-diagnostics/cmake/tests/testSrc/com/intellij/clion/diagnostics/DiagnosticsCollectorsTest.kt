package com.intellij.clion.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.cpp.diagnostics.collectCidrWorkspaces
import com.jetbrains.cidr.cpp.diagnostics.collectOCWorkspace
import com.jetbrains.cidr.cpp.diagnostics.collectToolchains
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for diagnostic collectors. We validate outputs by keywords rather than full golden text
 * to avoid flakiness due to machine-specific paths or versions.
 */
@TestApplication
class DiagnosticsCollectorsTest {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()

    private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
    private val project by cmakeProjectTestFixture(tempDir)
  }

  @Test
  fun testOCWorkspaceKeywords() {
    val out = collectOCWorkspace(project).toText()

    assertThat(out).contains(
      "Resolve configurations:",
      // There should be at least one configuration line
      "Configuration:",
      // We should list number of source files
      "source file(s)"
    )
  }

  @Test
  fun testCidrWorkspacesKeywords() {
    val out = collectCidrWorkspaces(project).toText()

    assertThat(out).contains(
      "Workspaces:",
      "Project path:",
      // Workspace provider adds toolchains list
      "Toolchains:"
    )
  }

  @Test
  fun testToolchainsKeywords() {
    val out = collectToolchains(project).toText()

    // Top-level system info
    assertThat(out).contains(
      "IDE:",
      "OS:",
      "Default toolchain:",

      // At least one toolchain block with basic fields
      "Toolchain:",
      "Kind:",
      "Path:"
    )
  }

  @Test
  fun testToolchainsOptionsKeywords() {
    val out = collectToolchains(project).toText()

    // Development options section should be printed with known keys
    assertThat(out).contains("Options:")
  }
}
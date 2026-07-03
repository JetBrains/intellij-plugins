package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for diagnostic collectors. We validate outputs by keywords rather than full golden text
 * to avoid flakiness due to machine-specific paths or versions.
 */
@TestApplication
class DiagnosticsCollectorsTest {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()
  }

  private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
  private val project by cmakeProjectTestFixture(tempDir)

  @Test
  fun testOCWorkspaceKeywords() {
    val out = collectOCWorkspace(project).toText()

    assertOutputContains(out, "Resolve configurations:")
    // There should be at least one configuration line
    assertOutputContains(out, "Configuration:")
    // We should list number of source files
    assertOutputContains(out, "source file(s)")
  }

  @Test
  fun testCidrWorkspacesKeywords() {
    val out = collectCidrWorkspaces(project).toText()

    assertOutputContains(out, "Workspaces:")
    assertOutputContains(out, "Project path:")
    // Workspace provider adds toolchains list
    assertOutputContains(out, "Toolchains:")
  }

  @Test
  fun testToolchainsKeywords() {
    val out = collectToolchains(project).toText()

    // Top-level system info
    assertOutputContains(out, "IDE:")
    assertOutputContains(out, "OS:")
    assertOutputContains(out, "Default toolchain:")

    // At least one toolchain block with basic fields
    assertOutputContains(out, "Toolchain:")
    assertOutputContains(out, "Kind:")
    assertOutputContains(out, "Path:")
  }

  @Test
  fun testToolchainsOptionsKeywords() {
    val out = collectToolchains(project).toText()

    // Development options section should be printed with known keys
    assertOutputContains(out, "Options:")
  }

  private fun assertOutputContains(text: String, needle: String) {
    assertTrue(text.contains(needle), "Expected to find '$needle' in output.\nOutput:\n$text")
  }
}
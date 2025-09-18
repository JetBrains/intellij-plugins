package com.jetbrains.cidr.cpp.diagnostics

import com.jetbrains.cidr.cpp.CPPTestCase

/**
 * Tests for diagnostic collectors. We validate outputs by keywords rather than full golden text
 * to avoid flakiness due to machine-specific paths or versions.
 */
class DiagnosticsCollectorsTest : CPPTestCase() {

  @Throws(Exception::class)
  override fun setUpFixtures() {
    super.setUpFixtures()
    // Use a tiny CMake project to initialize workspaces/OCWorkspace
    myProjectFixture.initProject("simple-cmake-project")
    myProjectFixture.waitForReloads()
  }

  fun testOCWorkspaceKeywords() {
    val out = collectOCWorkspace(project).toText()

    assertOutputContains(out, "Resolve configurations:")
    // There should be at least one configuration line
    assertOutputContains(out, "Configuration:")
    // We should list number of source files
    assertOutputContains(out, "source file(s)")
  }

  fun testCidrWorkspacesKeywords() {
    val out = collectCidrWorkspaces(project).toText()

    assertOutputContains(out, "Workspaces:")
    assertOutputContains(out, "Project path:")
    // Workspace provider adds toolchains list
    assertOutputContains(out, "Toolchains:")
  }

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

  fun testToolchainsOptionsKeywords() {
    val out = collectToolchains(project).toText()

    // Development options section should be printed with known keys
    assertOutputContains(out, "Options:")
  }

  private fun assertOutputContains(text: String, needle: String) {
    assertTrue("Expected to find '$needle' in output.\nOutput:\n$text", text.contains(needle))
  }
}
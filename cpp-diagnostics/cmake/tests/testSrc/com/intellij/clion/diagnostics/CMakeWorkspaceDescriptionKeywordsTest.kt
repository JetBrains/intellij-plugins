package com.intellij.clion.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.CompilerAndToolSet
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllLinuxToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllMacOSToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllWindowsToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.SupportedToolchainsProvider
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.toolchainFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.cpp.diagnostics.collectCidrWorkspaces
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.ArgumentsSource

@TestApplication
@ParameterizedClass
@RunWithAllLinuxToolchains
@RunWithAllMacOSToolchains
@RunWithAllWindowsToolchains
@ArgumentsSource(SupportedToolchainsProvider::class)
class CMakeWorkspaceDescriptionKeywordsTest(compilerAndToolSet: CompilerAndToolSet) {
  private val tempDir = tempDirTestFixture(cppDiagnosticsTestDataRoot.resolve("simple-cmake-project"))
  private val toolchainFixture = toolchainFixture(compilerAndToolSet)
  private val project by cmakeProjectTestFixture(tempDir, toolchainFixture)

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

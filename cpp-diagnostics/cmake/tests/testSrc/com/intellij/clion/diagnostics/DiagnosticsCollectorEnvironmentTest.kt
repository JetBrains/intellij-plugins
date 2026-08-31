package com.intellij.clion.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.cmake.cmakeProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.clionTimeoutRunBlocking
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.CompilerAndToolSet
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllLinuxToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllMacOSToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.RunWithAllWindowsToolchains
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.SupportedToolchainsProvider
import com.intellij.clion.testFramework.nolang.junit5.core.toolchains.toolchainFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.cpp.CPPTestUtil
import com.jetbrains.cidr.cpp.diagnostics.collectToolchains
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
class DiagnosticsCollectorEnvironmentTest(compilerAndToolSet: CompilerAndToolSet) {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()
  }

  private val toolchainFixture = toolchainFixture(compilerAndToolSet)

  private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
  private val project by cmakeProjectTestFixture(tempDir, toolchainFixture)

  @Test
  fun testEnvironmentFile(): Unit = clionTimeoutRunBlocking {
    CPPTestUtil.withEnvironmentReset {
      CPPTestUtil.changeTestToolchain { testToolchain ->
        testToolchain.environment = "/foo/bar"
      }

      val out = collectToolchains(project).toText()
      assertThat(out).contains("Environment file: /foo/bar")
    }
  }
}
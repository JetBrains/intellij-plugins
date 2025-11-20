package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.clion.testFramework.nolang.junit5.core.clionProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.cpp.CPPTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class DiagnosticsCollectorEnvironmentTest {
  companion object {
    private val projectDir = CidrTestDataFixture.getCppDiagnosticsTestData()
  }

  private val tempDir = tempDirTestFixture(projectDir.resolve("simple-cmake-project"))
  private val project by clionProjectTestFixture(tempDir)

  @Test
  fun testEnvironmentFile() {
    val environment = CPPTestUtil.getTestToolchain().environment

    try {
      CPPTestUtil.changeTestToolchain { testToolchain ->
        testToolchain.environment = "/foo/bar"
      }

      val out = collectToolchains(project).toText()
      assertThat(out).contains("Environment file: /foo/bar")
    }
    finally {
      CPPTestUtil.changeTestToolchain { testToolchain ->
        testToolchain.environment = environment
      }
    }
  }
}
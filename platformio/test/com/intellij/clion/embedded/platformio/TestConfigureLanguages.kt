package com.intellij.clion.embedded.platformio

import com.intellij.clion.testFramework.nolang.junit5.core.clionProjectTestFixture
import com.intellij.openapi.components.service
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioProjectResolver
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@TestApplication
internal class TestConfigureLanguages {

  private val dirFixture = tempPathFixture()

  private val dir by dirFixture
  private val project by clionProjectTestFixture(dirFixture)

  @Test
  fun testSwitchInQuotes() {
    val builder = ExternalResolveConfigurationBuilder("test-env", "PlatformIO", dir.toFile())
    val workspace = project.service<PlatformioWorkspace>()

    val jsonConfig = mapOf(
      "compiler_type" to "gcc",
      "cc_flags" to "\"-DCHIP_ADDRESS_RESOLVE_IMPL_INCLUDE_HEADER=<lib/address_resolve/AddressResolve_DefaultImpl.h>\"",
    )

    val languages = PlatformioProjectResolver.configureLanguages(jsonConfig, builder, workspace)

    val cLanguage = languages.single { !it.languageKind.isCpp }

    // Assert the switch is properly parsed and has the correct content when unescaped
    val unescapedSwitches = cLanguage.compilerSwitches
    assertThat(unescapedSwitches).contains("-DCHIP_ADDRESS_RESOLVE_IMPL_INCLUDE_HEADER=<lib/address_resolve/AddressResolve_DefaultImpl.h>")
  }

  @Test
  fun testSpaceInIncludePath() {
    val builder = ExternalResolveConfigurationBuilder("test-env", "PlatformIO", dir.toFile())
    val workspace = project.service<PlatformioWorkspace>()

    val pathString = Path("path", "to", "include", "with space").toString()

    val jsonConfig = mapOf(
      "compiler_type" to "gcc",
      "includes" to mapOf(
        "include_paths" to listOf(pathString)
      ),
    )

    val languages = PlatformioProjectResolver.configureLanguages(jsonConfig, builder, workspace)

    val cLanguage = languages.single { !it.languageKind.isCpp }

    val unescapedSwitches = cLanguage.compilerSwitches
    assertThat(unescapedSwitches).contains("-I$pathString")
  }

  @Test
  fun testSwitchWithQuotes() {
    val builder = ExternalResolveConfigurationBuilder("test-env", "PlatformIO", dir.toFile())
    val workspace = project.service<PlatformioWorkspace>()

    val jsonConfig = mapOf(
      "compiler_type" to "gcc",
      "defines" to listOf("BOARD_NAME=\"AFROFLIGHT_F103CB\""),
    )

    val languages = PlatformioProjectResolver.configureLanguages(jsonConfig, builder, workspace)

    val cLanguage = languages.single { !it.languageKind.isCpp }

    // Assert the switch is properly parsed and has the correct content when unescaped
    val unescapedSwitches = cLanguage.compilerSwitches
    assertThat(unescapedSwitches).contains("-DBOARD_NAME=\"AFROFLIGHT_F103CB\"")
  }
}

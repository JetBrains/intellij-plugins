// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.prettierjs.PrettierUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Checks the config file name rule of [PrettierUtil].
 * The listener that terminates the Prettier services uses this rule, and other Prettier features use it too.
 */
@TestApplication
class PrettierUtilConfigFileTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val myFixture by sharedCodeInsightFixture(projectFixture)

  @Test
  fun testJsConfigFileNames() = checkConfigFiles(JS_CONFIG_FILE_NAMES, isJs = true, isNonJs = false)

  @Test
  fun testTsConfigFileNames() = checkConfigFiles(TS_CONFIG_FILE_NAMES, isJs = false, isNonJs = false)

  @Test
  fun testDataConfigFileNames() = checkConfigFiles(DATA_CONFIG_FILE_NAMES, isJs = false, isNonJs = true)

  @Test
  fun testOtherFileNames() {
    for (name in OTHER_FILE_NAMES) {
      val file = LightVirtualFile(name)
      assertFalse(PrettierUtil.isConfigFileName(name), name)
      assertFalse(PrettierUtil.isConfigFile(file), name)
      assertFalse(PrettierUtil.isJSConfigFile(file), name)
      assertFalse(PrettierUtil.isNonJSConfigFile(file), name)
    }
  }

  @Test
  fun testPackageJson() {
    val file = LightVirtualFile("package.json")
    assertFalse(PrettierUtil.isConfigFile(file))
    assertTrue(PrettierUtil.isConfigFileOrPackageJson(file))
    // A directory with the name package.json is not a package.json file.
    assertFalse(PrettierUtil.isConfigFileOrPackageJson(myFixture.tempDirFixture.findOrCreateDir("package.json")))
  }

  @Test
  fun testNoFile() {
    assertFalse(PrettierUtil.isConfigFile(null as VirtualFile?))
    assertFalse(PrettierUtil.isConfigFileOrPackageJson(null))
  }

  private fun checkConfigFiles(names: List<String>, isJs: Boolean, isNonJs: Boolean) {
    for (name in names) {
      val file = LightVirtualFile(name)
      assertTrue(PrettierUtil.isConfigFileName(name), name)
      assertTrue(PrettierUtil.isConfigFile(file), name)
      assertTrue(PrettierUtil.isConfigFileOrPackageJson(file), name)
      assertEquals(isJs, PrettierUtil.isJSConfigFile(file), name)
      assertEquals(isNonJs, PrettierUtil.isNonJSConfigFile(file), name)
    }
  }
}

// The Prettier configuration file names. See https://prettier.io/docs/configuration.

private val JS_CONFIG_FILE_NAMES = listOf(
  ".prettierrc.js", "prettier.config.js", ".prettierrc.mjs", "prettier.config.mjs", ".prettierrc.cjs", "prettier.config.cjs",
)

private val TS_CONFIG_FILE_NAMES = listOf(
  ".prettierrc.ts", "prettier.config.ts", ".prettierrc.mts", "prettier.config.mts", ".prettierrc.cts", "prettier.config.cts",
)

private val DATA_CONFIG_FILE_NAMES = listOf(
  ".prettierrc", ".prettierrc.json", ".prettierrc.yml", ".prettierrc.yaml", ".prettierrc.json5", ".prettierrc.toml",
)

private val OTHER_FILE_NAMES = listOf(
  "package.json", ".editorconfig", ".prettierignore", "prettier.config.json", "prettier.config", "prettierrc", ".prettierrc5",
  ".prettierrc.json.bak", "index.js",
)

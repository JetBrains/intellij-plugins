// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.PrettierUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Checks the config file name rule of [PrettierUtil].
 * The listener that terminates the Prettier services uses this rule, and other Prettier features use it too.
 */
class PrettierUtilConfigFileTest : BasePlatformTestCase() {
  fun testJsConfigFileNames() = checkConfigFiles(JS_CONFIG_FILE_NAMES, isJs = true, isNonJs = false)

  fun testTsConfigFileNames() = checkConfigFiles(TS_CONFIG_FILE_NAMES, isJs = false, isNonJs = false)

  fun testDataConfigFileNames() = checkConfigFiles(DATA_CONFIG_FILE_NAMES, isJs = false, isNonJs = true)

  fun testOtherFileNames() {
    for (name in OTHER_FILE_NAMES) {
      val file = LightVirtualFile(name)
      assertFalse(name, PrettierUtil.isConfigFileName(name))
      assertFalse(name, PrettierUtil.isConfigFile(file))
      assertFalse(name, PrettierUtil.isJSConfigFile(file))
      assertFalse(name, PrettierUtil.isNonJSConfigFile(file))
    }
  }

  fun testPackageJson() {
    val file = LightVirtualFile("package.json")
    assertFalse(PrettierUtil.isConfigFile(file))
    assertTrue(PrettierUtil.isConfigFileOrPackageJson(file))
    // A directory with the name package.json is not a package.json file.
    assertFalse(PrettierUtil.isConfigFileOrPackageJson(myFixture.tempDirFixture.findOrCreateDir("package.json")))
  }

  fun testNoFile() {
    assertFalse(PrettierUtil.isConfigFile(null as VirtualFile?))
    assertFalse(PrettierUtil.isConfigFileOrPackageJson(null))
  }

  private fun checkConfigFiles(names: List<String>, isJs: Boolean, isNonJs: Boolean) {
    for (name in names) {
      val file = LightVirtualFile(name)
      assertTrue(name, PrettierUtil.isConfigFileName(name))
      assertTrue(name, PrettierUtil.isConfigFile(file))
      assertTrue(name, PrettierUtil.isConfigFileOrPackageJson(file))
      assertEquals(name, isJs, PrettierUtil.isJSConfigFile(file))
      assertEquals(name, isNonJs, PrettierUtil.isNonJSConfigFile(file))
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

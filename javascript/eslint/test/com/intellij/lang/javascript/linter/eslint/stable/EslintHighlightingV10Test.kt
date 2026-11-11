// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.Language
import com.intellij.lang.javascript.linter.eslint.ESLINT_10_6_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * The primary pinned ESLint highlighting suite: flat config on eslint 10.6.0. Runs every
 * generic scenario plus the flat-config-specific ones. A failure here is a product regression.
 */
@TestApplication
@TestNpmPackage(ESLINT_10_6_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV10Test : EslintHighlightingGenericTest() {
  @Test
  fun testFlatConfigOneDir(): Unit = eslint.doHighlightingTestWithInstallation("index.js")

  @Test
  fun testFlatConfigSubdirs(): Unit = eslint.doHighlightingTestWithInstallation("src/sub/dir/index.js")

  @Test
  fun testTypescript(): Unit = eslint.doHighlightingTestWithInstallation("ts.ts")

  @Test
  fun testVueFile() {
    assertNotNull(Language.findLanguageByID("Vue"), "This test must be run with intellij.vuejs module in classpath")
    eslint.doHighlightingTestWithInstallation("vue.vue")
  }

  @Test
  fun testVueTsFile() {
    assertNotNull(Language.findLanguageByID("Vue"), "This test must be run with intellij.vuejs module in classpath")
    eslint.doHighlightingTestWithInstallation("vue.vue")
  }

  @Test
  fun testFlatConfigNoHtmlPlugin(): Unit = eslint.doHighlightingTestWithInstallation("index.html")

  // eslint.config.mts loaded via jiti; eslint 10 loads TS configs natively (no unstable flag needed).
  @Test
  fun testFlatTypescriptConfigOneDir(): Unit = eslint.doHighlightingTestWithInstallation("index.js")

  // A custom (non-standard-named) flat config selected via the "custom config file" setting.
  @Test
  fun testCustomFlatConfig(): Unit = eslint.doHighlightingTestWithInstallation("index.js") {
    eslint.useCustomConfigFile("eslint.config.fast.mjs")
  }

  // ESLint autodetected from the project-local node_modules + a flat eslint.config.js.
  @Test
  fun testCanAutodetectInstalledLocalPackage(): Unit =
    eslint.doHighlightingTestWithAutodetectInstallation("js.js")

  // A file ignored via the --ignore-pattern CLI option is skipped (flat config; option is v10-supported).
  @Test
  fun testFileIgnoredByCommandLineOption(): Unit =
    eslint.doHighlightingTestWithInstallation("testIgnored.js") {
      eslint.updateConfiguration { it.setExtraOptions("--ignore-pattern '*.js'") }
    }

  // Autodetect the project-local ESLint (declared dependency) + a flat eslint.config.mjs.
  @Test
  fun testCanAutodetectLocalPackage(): Unit = eslint.doHighlightingTestWithAutodetectInstallation("js.js")

  // Autodetect ESLint from a parent directory's node_modules for a file in a sub-workspace.
  @Test
  fun testCanAutodetectLocalPackageInParentNodeModules(): Unit =
    eslint.doHighlightingTestWithAutodetectInstallation("workspaces/a/js.js")

  // A sub-package with no ESLint of its own resolves the parent's (walking up node_modules), then lints
  // with its own flat config.
  @Test
  fun testSubpackageContainsOnlyLinkToParentEslint(): Unit =
    eslint.doHighlightingTestWithAutodetectInstallation("packages/inner/js.js")

  // A flat config that imports a sibling local config file (the flat-config analog of eslintrc extends).
  @Test
  fun testConfigReferencesLocalFiles(): Unit = eslint.doHighlightingTestWithAutodetectInstallation("packages/a/js.js")

  // Per-subdirectory TS flat configs (.cts/.ts/.mts), each loaded via jiti; eslint resolves the nearest config per file.
  @Test
  fun testFlatTypescriptConfigSubDirs() {
    // installEslintForTest copies the whole dir once; each pass highlights a different file, so use the
    // WithoutCopy variant to avoid re-copying (which clobbers the substituted package.json).
    eslint.installEslintForTest()
    eslint.doEditorHighlightingTestWithoutCopy("src/index.js")
    eslint.doEditorHighlightingTestWithoutCopy("src/sub/index.js")
    eslint.doEditorHighlightingTestWithoutCopy("src/sub/dir/index.js")
  }

  // Mixed TS (.cts/.mts) and JS (.mjs) flat configs across subdirectories.
  @Test
  fun testFlatMixedConfigSubDirs() {
    eslint.installEslintForTest()
    eslint.doEditorHighlightingTestWithoutCopy("src/index.js")
    eslint.doEditorHighlightingTestWithoutCopy("src/sub/index.js")
    eslint.doEditorHighlightingTestWithoutCopy("src/sub/dir/index.js")
  }

  // @html-eslint plugin with the `html/html` language (flat config via defineConfig from eslint/config).
  @Test
  fun testHtmlFileFlatConfig(): Unit = eslint.doHighlightingTestWithInstallation("index.html")

  // @html-eslint flat/recommended preset spread into the config.
  @Test
  fun testHtmlFileFlatConfigFromLib(): Unit = eslint.doHighlightingTestWithInstallation("index.html")
}

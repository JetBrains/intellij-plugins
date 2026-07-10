// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.Language
import com.intellij.lang.javascript.linter.eslint.ESLINT_10_6_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.modules.TestNpmPackage
import org.junit.Assert

/**
 * The primary pinned ESLint highlighting suite: flat config on eslint 10.6.0. Runs every
 * generic scenario plus the flat-config-specific ones. A failure here is a product regression.
 */
@TestNpmPackage(ESLINT_10_6_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV10Test : EslintHighlightingGenericTest() {
  fun testFlatConfigOneDir() = doHighlightingTestWithInstallation("index.js")

  fun testFlatConfigSubdirs() = doHighlightingTestWithInstallation("src/sub/dir/index.js")

  fun testTypescript() = doHighlightingTestWithInstallation("ts.ts")

  fun testVueFile() {
    Assert.assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"))
    doHighlightingTestWithInstallation("vue.vue")
  }

  fun testVueTsFile() {
    Assert.assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"))
    doHighlightingTestWithInstallation("vue.vue")
  }

  fun testFlatConfigNoHtmlPlugin() = doHighlightingTestWithInstallation("index.html")

  // eslint.config.mts loaded via jiti; eslint 10 loads TS configs natively (no unstable flag needed).
  fun testFlatTypescriptConfigOneDir() = doHighlightingTestWithInstallation("index.js")

  // A custom (non-standard-named) flat config selected via the "custom config file" setting.
  fun testCustomFlatConfig() = doHighlightingTestWithInstallation("index.js") {
    useCustomConfigFile("eslint.config.fast.mjs")
  }

  // ESLint autodetected from the project-local node_modules + a flat eslint.config.js.
  fun testCanAutodetectInstalledLocalPackage() =
    doHighlightingTestWithAutodetectInstallation("js.js")

  // A file ignored via the --ignore-pattern CLI option is skipped (flat config; option is v10-supported).
  fun testFileIgnoredByCommandLineOption() =
    doHighlightingTestWithInstallation("testIgnored.js") {
      updateConfiguration { it.setExtraOptions("--ignore-pattern '*.js'") }
    }

  // Autodetect the project-local ESLint (declared dependency) + a flat eslint.config.mjs.
  fun testCanAutodetectLocalPackage() = doHighlightingTestWithAutodetectInstallation("js.js")

  // Autodetect ESLint from a parent directory's node_modules for a file in a sub-workspace.
  fun testCanAutodetectLocalPackageInParentNodeModules() =
    doHighlightingTestWithAutodetectInstallation("workspaces/a/js.js")

  // A sub-package with no ESLint of its own resolves the parent's (walking up node_modules), then lints
  // with its own flat config.
  fun testSubpackageContainsOnlyLinkToParentEslint() =
    doHighlightingTestWithAutodetectInstallation("packages/inner/js.js")

  // A flat config that imports a sibling local config file (the flat-config analog of eslintrc extends).
  fun testConfigReferencesLocalFiles() = doHighlightingTestWithAutodetectInstallation("packages/a/js.js")

  // Per-subdirectory TS flat configs (.cts/.ts/.mts), each loaded via jiti; eslint resolves the nearest config per file.
  fun testFlatTypescriptConfigSubDirs() {
    // installEslintForTest copies the whole dir once; each pass highlights a different file, so use the
    // WithoutCopy variant to avoid re-copying (which clobbers the substituted package.json).
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy("src/index.js")
    doEditorHighlightingTestWithoutCopy("src/sub/index.js")
    doEditorHighlightingTestWithoutCopy("src/sub/dir/index.js")
  }

  // Mixed TS (.cts/.mts) and JS (.mjs) flat configs across subdirectories.
  fun testFlatMixedConfigSubDirs() {
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy("src/index.js")
    doEditorHighlightingTestWithoutCopy("src/sub/index.js")
    doEditorHighlightingTestWithoutCopy("src/sub/dir/index.js")
  }

  // @html-eslint plugin with the `html/html` language (flat config via defineConfig from eslint/config).
  fun testHtmlFileFlatConfig() = doHighlightingTestWithInstallation("index.html")

  // @html-eslint flat/recommended preset spread into the config.
  fun testHtmlFileFlatConfigFromLib() = doHighlightingTestWithInstallation("index.html")
}

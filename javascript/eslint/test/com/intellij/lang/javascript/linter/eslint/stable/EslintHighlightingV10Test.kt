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

  // Per-subdirectory TS flat configs (.cts/.ts/.mts), each loaded via jiti; eslint resolves the nearest config per file.
  fun testFlatTypescriptConfigSubDirs() {
    installEslintForTest()
    doEditorHighlightingTest("src/index.js")
    doEditorHighlightingTest("src/sub/index.js")
    doEditorHighlightingTest("src/sub/dir/index.js")
  }

  // Mixed TS (.cts/.mts) and JS (.mjs) flat configs across subdirectories.
  fun testFlatMixedConfigSubDirs() {
    installEslintForTest()
    doEditorHighlightingTest("src/index.js")
    doEditorHighlightingTest("src/sub/index.js")
    doEditorHighlightingTest("src/sub/dir/index.js")
  }
}

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_9_11_1_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.modules.TestNpmPackage

/**
 * Pinned to eslint 9.11.1 -- the only major where "fall back to a legacy `.eslintrc` config" is a real,
 * exercisable code path: eslint 9 defaults to flat config, but when only an eslintrc file is present the
 * IDE plugin loads `LegacyESLint` from `../lib/unsupported-api` (eslint8-plugin.ts). On eslint 8 eslintrc
 * IS the default (no fallback) and eslint 10 dropped `LegacyESLint`, so this scenario belongs on neither
 * the V8 legacy nor the V10 pinned class.
 */
@TestNpmPackage(ESLINT_9_11_1_TEST_PACKAGE_SPEC)
class EslintFallbackToLegacyConfigV9Test : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/"

  fun testFallbackToLegacyConfig() = doHighlightingTestWithInstallation("index.js")
}

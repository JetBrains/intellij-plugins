// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_9_39_5_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockFixture.Companion.eslintPackageLockFixture
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

/**
 * Bulk suppressions on eslint 9.39.5. ESLint 9.24 - 10.0 applies the suppressions file in the CLI only, so the plugin
 * post-processes the results with ESLint's internal `SuppressionsService` (eslint8-plugin.ts). ESLint 10.1+ applies the
 * file itself through the `applySuppressions` option. [EslintHighlightingGenericTest] covers that path with the same scenarios.
 */
@TestApplication
@TestNpmPackage(ESLINT_9_39_5_TEST_PACKAGE_SPEC)
class EslintBulkSuppressionsV9Test {
  private val eslint by eslintPackageLockFixture("$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/")

  @Test
  fun testBulkSuppressions(): Unit = eslint.doHighlightingTestWithInstallation("index.js")

  @Test
  fun testBulkSuppressionsCountExceeded(): Unit = eslint.doHighlightingTestWithInstallation("index.js")

  @Test
  fun testBulkSuppressionsCustomLocation(): Unit = eslint.doHighlightingTestWithInstallation("index.js") {
    eslint.updateConfiguration { it.setExtraOptions("--suppressions-location custom-suppressions.json") }
  }
}

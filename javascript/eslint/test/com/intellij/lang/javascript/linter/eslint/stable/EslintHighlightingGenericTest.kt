// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase

/**
 * Version-agnostic ESLint highlighting scenarios, shared by the pinned `stable` class
 * ([EslintHighlightingV10Test]) and the `next` canary. Scenarios here must pass on every
 * supported version, so they assert on rule ids / highlight ranges and version-stable
 * messages. Version-specific scenarios (exact-message goldens, flat-config-only behavior)
 * belong in the pinned subclass.
 *
 * Subclasses must be annotated with `@`[com.intellij.lang.javascript.modules.TestNpmPackage].
 */
abstract class EslintHighlightingGenericTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String =
    ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/highlighting/"

  fun testWarningsAndErrors() {
    doHighlightingTestWithInstallation("warn.js")
    doBatchInspectionTest()
  }
}

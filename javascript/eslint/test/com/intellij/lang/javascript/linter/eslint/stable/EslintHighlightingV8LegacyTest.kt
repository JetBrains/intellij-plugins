// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_8_57_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.modules.TestNpmPackage

/**
 * Legacy `.eslintrc`-only ESLint scenarios, pinned to eslint 8.57.0 -- the last release that supports
 * eslintrc configs (removed in eslint 9/10). These exercise behavior with no flat-config equivalent
 * (string parser resolution, eslintrc parse/fatal errors), so they stay on a frozen eslint 8 rather
 * than migrating to flat config. eslint 8.57.0 never changes, so these goldens are stable.
 */
@TestNpmPackage(ESLINT_8_57_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV8LegacyTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/"

  private fun doTest(mainFile: String) {
    doHighlightingTestWithInstallation(mainFile)
    doBatchInspectionTest()
  }

  fun testReportAboutWrongParser() = doTest("test.js")

  fun testESLintLocalFatalError() = doTest("test.js")

  fun testESLintGlobalFatalError() {
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: Error: Failed to load parser 'babel'", true, false)
    doHighlightingTestWithInstallation("test.jsx")
  }

  // Legacy ignore mechanisms removed in flat config: .eslintignore and package.json "eslintIgnore".
  fun testFileIgnored() = doTest("testIgnored.js")

  fun testFileIgnoredByCommandLineOption() =
    doHighlightingTestWithInstallation("testIgnored.js") {
      updateConfiguration { it.setExtraOptions("--ignore-pattern '*.js'") }
    }

  fun testFileIgnoredWithPackageJsonOption() = doHighlightingTestWithInstallation("src/ignoredDir/test.js")

  fun testCanDisableIgnoreFilesWithCommandLineOption() =
    doHighlightingTestWithInstallation("test.js") {
      updateConfiguration { it.setExtraOptions("--no-ignore") }
    }
}

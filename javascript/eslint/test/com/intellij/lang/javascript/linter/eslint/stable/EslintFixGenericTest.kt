// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager

/**
 * Version-agnostic ESLint quick-fix and suppression scenarios, reused by the pinned [EslintFixV10Test]
 * (and, later, the next-tier canary).
 *
 * These tests historically shared a single `.eslintrc`; the flat-config successor is a shared
 * `eslint.config.mjs` copied to the project root and auto-detected by ESLint. ESLint itself is
 * installed from a shared root `package.json` (the full TS/Vue/HTML combo), so a single lock store
 * serves the whole suite regardless of which plugin a given test exercises.
 */
abstract class EslintFixGenericTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/quickfix/"

  /**
   * Copies the shared root `package.json` + `eslint.config.mjs` into the project, installs ESLint from
   * the stored lock, and lets the flat config be auto-detected. Call once per test, before configuring
   * the file under test.
   */
  protected fun installEslintWithSharedConfig() {
    WriteAction.run<Throwable> { FileDocumentManager.getInstance().saveAllDocuments() }
    myFixture.setCaresAboutInjection(false)
    myFixture.copyFileToProject("package.json")
    myFixture.copyFileToProject("eslint.config.mjs")
    installEslintFromProjectRoot()
  }

  /**
   * Shared-config install, then configure `<TestName><extension>`, launch the intention named
   * [description], and compare the result with `<TestName>_after<extension>`.
   */
  protected fun doQuickFixTest(description: String, extension: String = ".js") {
    installEslintWithSharedConfig()
    myFixture.configureByFile(getTestName(false) + extension)
    val intention = myFixture.getAvailableIntention(description)
                    ?: error("Intention '$description' is not available")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile(getTestName(false) + "_after" + extension)
  }

  fun testSuppressByLineComment() = doQuickFixTest("Suppress 'comma-spacing' for current line")

  fun testSuppressByFileComment() = doQuickFixTest("Suppress 'comma-spacing' for current file")

  fun testAddSuppressionToExistingLineComment() = doQuickFixTest("Suppress 'comma-spacing' for current line")

  fun testAddSuppressionToExistingFileComment() = doQuickFixTest("Suppress 'comma-spacing' for current file")

  fun testSuppressAllRulesForFile() = doQuickFixTest("Suppress all ESLint rules for current file")

  fun testSuppressAllRulesForFileWithExistingComment() = doQuickFixTest("Suppress all ESLint rules for current file")

  fun testSuppressForLineInNestedScopeWithIndent() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { settings ->
      settings.getCommonSettings(JavascriptLanguage).LINE_COMMENT_AT_FIRST_COLUMN = false
      doQuickFixTest("Suppress 'comma-spacing' for current line")
    }
  }

  fun testSuppressForLineInNestedScopeAtLineStart() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { settings ->
      settings.getCommonSettings(JavascriptLanguage).LINE_COMMENT_AT_FIRST_COLUMN = true
      doQuickFixTest("Suppress 'comma-spacing' for current line")
    }
  }
}

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.Assert

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
    launchQuickFix(description, extension)
  }

  /**
   * Configures `<TestName><extension>`, launches the intention named [description], and compares the
   * result with `<TestName>_after<extension>`. Assumes ESLint is already installed (see
   * [installEslintWithSharedConfig]) -- split out so dumb-mode tests can install before entering dumb mode.
   */
  protected fun launchQuickFix(description: String, extension: String = ".js") {
    myFixture.configureByFile(getTestName(false) + extension)
    val intention = myFixture.getAvailableIntention(description)
                    ?: error("Intention '$description' is not available")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile(getTestName(false) + "_after" + extension)
  }

  /**
   * For tests whose data lives in a per-test directory with its own `package.json` + flat config
   * (rather than the shared root config): installs from that directory, highlights
   * `<mainFileName><extension>`, launches [description], and checks `<mainFileName>_after<extension>`.
   */
  protected fun doQuickFixTestForDirectory(description: String, mainFileName: String, extension: String) {
    installEslintForTest()
    doFixTestForDirectory(mainFileName, extension, description)
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

  fun testFixFileWorks() = doQuickFixTest("ESLint: Fix current file")

  fun testFixWithSearchForConfig() = doQuickFixTest("ESLint: Fix current file")

  fun testFixFileInDumbWorks() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, testRootDisposable)
    installEslintWithSharedConfig()
    DumbModeTestUtils.runInDumbModeSynchronously(project) {
      launchQuickFix("ESLint: Fix current file")
    }
  }

  fun testSuppressForLineInJSXTagContent() =
    doQuickFixTestForDirectory("Suppress 'react/self-closing-comp' for current line", "test", ".jsx")

  fun testSuppressForLineInJSXTagContent2() =
    doQuickFixTestForDirectory("Suppress 'jsx-quotes' for current line", "test", ".jsx")

  fun testSuppressForLineInJSXTagContentAddsToExistingComment() =
    doQuickFixTestForDirectory("Suppress 'react/no-unknown-property' for current line", "test", ".jsx")

  fun testSuppressForLineInJSXTagAttributes() =
    doQuickFixTestForDirectory("Suppress 'react/jsx-curly-brace-presence' for current line", "test", ".jsx")

  fun testFixFileInVue() {
    Assert.assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"))
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue")
  }

  fun testFixFileInVueTs() {
    Assert.assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"))
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue")
  }
}

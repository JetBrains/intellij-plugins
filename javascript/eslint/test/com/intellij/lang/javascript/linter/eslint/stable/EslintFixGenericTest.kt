// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockFixture.Companion.eslintPackageLockFixture
import com.intellij.lang.javascript.linter.eslint.EslintState
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.util.LineSeparator
import com.intellij.util.ThrowableRunnable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Version-agnostic ESLint quick-fix and suppression scenarios, reused by the pinned [EslintFixV10Test]
 * (and, later, the next-tier canary).
 *
 * These tests historically shared a single `.eslintrc`; the flat-config successor is a shared
 * `eslint.config.mjs` copied to the project root and auto-detected by ESLint. ESLint is installed from
 * one of two committed root manifests, chosen per test via [EslintPackageSet]: pure-JS scenarios use the
 * eslint-only `package.json` (a fast single-dep lock), while scenarios whose config loads a plugin or
 * parser (HTML, JSX) use the TS/Vue/HTML/React combo `package.full.json`.
 */
abstract class EslintFixGenericTest {
  protected val eslint by eslintPackageLockFixture("$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/quickfix/")

  /**
   * Which committed root manifest a shared-config install pulls its dependencies from. Pure-JS scenarios
   * use [CORE] (eslint only -> the fast single-dep lock store); scenarios whose flat config loads a plugin
   * or parser (HTML, JSX) use [FULL] (the TS/Vue/HTML/React combo).
   */
  protected enum class EslintPackageSet(val rootManifest: String) {
    CORE("package.json"),
    FULL("package.full.json"),
  }

  /**
   * Copies the chosen root manifest (as `package.json`) + `eslint.config.mjs` into the project, installs
   * ESLint from the stored lock, and lets the flat config be auto-detected. Call once per test, before
   * configuring the file under test.
   */
  protected fun installEslintWithSharedConfig(
    configSource: String = "eslint.config.mjs",
    packageSet: EslintPackageSet = EslintPackageSet.CORE,
  ) {
    prepareProjectForInstall()
    eslint.fixture.copyFileToProject(packageSet.rootManifest, "package.json")
    eslint.fixture.copyFileToProject(configSource, "eslint.config.mjs")
    eslint.installEslintFromProjectRoot()
  }

  /** Installs ESLint from the eslint-only root manifest without any flat config -- for tests that add
   *  their own config (e.g. run-on-save with an inline `eslint.config.js`). */
  protected fun installEslintOnly() {
    prepareProjectForInstall()
    eslint.fixture.copyFileToProject(EslintPackageSet.CORE.rootManifest, "package.json")
    eslint.installEslintFromProjectRoot()
  }

  private fun prepareProjectForInstall() {
    eslint.prepareFixtureForHighlighting()
  }

  /**
   * Shared-config install (from [configSource], copied to the project as `eslint.config.mjs`, with
   * dependencies from [packageSet]), then configure `<TestName><extension>`, launch the intention named
   * [description], and compare the result with `<TestName>_after<extension>`.
   */
  protected fun doQuickFixTest(
    description: String,
    extension: String = ".js",
    configSource: String = "eslint.config.mjs",
    packageSet: EslintPackageSet = EslintPackageSet.CORE,
  ) {
    installEslintWithSharedConfig(configSource, packageSet)
    launchQuickFix(description, extension)
  }

  /**
   * Configures `<TestName><extension>`, launches the intention named [description], and compares the
   * result with `<TestName>_after<extension>`. Assumes ESLint is already installed (see
   * [installEslintWithSharedConfig]) -- split out so dumb-mode tests can install before entering dumb mode.
   */
  protected fun launchQuickFix(description: String, extension: String = ".js") {
    eslint.fixture.configureByFile(eslint.getTestName(false) + extension)
    val intention = eslint.fixture.getAvailableIntention(description)
                    ?: error("Intention '$description' is not available")
    eslint.fixture.launchAction(intention)
    eslint.fixture.checkResultByFile(eslint.getTestName(false) + "_after" + extension)
  }

  /**
   * For tests whose data lives in a per-test directory with its own `package.json` + flat config
   * (rather than the shared root config): installs from that directory, highlights
   * `<mainFileName><extension>`, launches [description], and checks `<mainFileName>_after<extension>`.
   */
  protected fun doQuickFixTestForDirectory(
    description: String,
    mainFileName: String,
    extension: String,
    lineSeparator: LineSeparator? = null,
  ) {
    eslint.installEslintForTest()
    // installEslintForTest already copied the test directory; use the WithoutCopy variant so the fix run
    // does not re-copy (which would clobber the substituted package.json with the raw $ESLINT_VERSION$).
    eslint.doFixTestForDirectoryWithoutCopy(mainFileName, extension, description, ThrowableRunnable<Throwable> {
      if (lineSeparator != null) {
        JSTestUtils.ensureLineSeparators(eslint.fixture.file, lineSeparator)
      }
    })
  }

  @Test
  fun testSuppressByLineComment(): Unit = doQuickFixTest("Suppress 'comma-spacing' for current line")

  @Test
  fun testSuppressByFileComment(): Unit = doQuickFixTest("Suppress 'comma-spacing' for current file")

  @Test
  fun testAddSuppressionToExistingLineComment(): Unit = doQuickFixTest("Suppress 'comma-spacing' for current line")

  @Test
  fun testAddSuppressionToExistingFileComment(): Unit = doQuickFixTest("Suppress 'comma-spacing' for current file")

  @Test
  fun testSuppressAllRulesForFile(): Unit = doQuickFixTest("Suppress all ESLint rules for current file")

  @Test
  fun testSuppressAllRulesForFileWithExistingComment(): Unit = doQuickFixTest("Suppress all ESLint rules for current file")

  @Test
  fun testSuppressForLineInNestedScopeWithIndent() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(eslint.project) { settings ->
      settings.getCommonSettings(JavascriptLanguage).LINE_COMMENT_AT_FIRST_COLUMN = false
      doQuickFixTest("Suppress 'comma-spacing' for current line")
    }
  }

  @Test
  fun testSuppressForLineInNestedScopeAtLineStart() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(eslint.project) { settings ->
      settings.getCommonSettings(JavascriptLanguage).LINE_COMMENT_AT_FIRST_COLUMN = true
      doQuickFixTest("Suppress 'comma-spacing' for current line")
    }
  }

  @Test
  fun testFixFileWorks(): Unit = doQuickFixTest("ESLint: Fix current file")

  @Test
  fun testFixWithSearchForConfig(): Unit = doQuickFixTest("ESLint: Fix current file")

  @Test
  fun testFixFileInDumbWorks() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, eslint.testRootDisposable)
    installEslintWithSharedConfig()
    DumbModeTestUtils.runInDumbModeSynchronously(eslint.project) {
      launchQuickFix("ESLint: Fix current file")
    }
  }

  @Test
  fun testSuppressForLineInJSXTagContent(): Unit =
    doQuickFixTestForDirectory("Suppress 'react/self-closing-comp' for current line", "test", ".jsx")

  @Test
  fun testSuppressForLineInJSXTagContent2(): Unit =
    doQuickFixTestForDirectory("Suppress 'jsx-quotes' for current line", "test", ".jsx")

  @Test
  fun testSuppressForLineInJSXTagContentAddsToExistingComment(): Unit =
    doQuickFixTestForDirectory("Suppress 'react/no-unknown-property' for current line", "test", ".jsx")

  @Test
  fun testSuppressForLineInJSXTagAttributes(): Unit =
    doQuickFixTestForDirectory("Suppress 'react/jsx-curly-brace-presence' for current line", "test", ".jsx")

  @Test
  fun testFixFileInVue() {
    assertNotNull(Language.findLanguageByID("Vue"), "This test must be run with intellij.vuejs module in classpath")
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue")
  }

  @Test
  fun testFixFileInVueTs() {
    assertNotNull(Language.findLanguageByID("Vue"), "This test must be run with intellij.vuejs module in classpath")
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue")
  }

  @Test
  fun testFixSingleError(): Unit =
    doQuickFixTestForDirectory(fixProblemsDescription("no-multiple-empty-lines"), "test", ".js")

  @Test
  fun testFixSingleErrorWithWindowsLineSeparators() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory(fixProblemsDescription("no-multiple-empty-lines"), "test", ".js", LineSeparator.CRLF)
  }

  @Test
  fun testFixFileWithWindowsLineSeparator() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.CRLF)
    // saveAllDocuments needs the EDT.
    timeoutRunBlocking { edtWriteAction { FileDocumentManager.getInstance().saveAllDocuments() } }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(eslint.fixture.file.virtualFile)))
  }

  @Test
  fun testFixFileWithConvertLineSeparators() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.LF)
    // saveAllDocuments needs the EDT.
    timeoutRunBlocking { edtWriteAction { FileDocumentManager.getInstance().saveAllDocuments() } }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(eslint.fixture.file.virtualFile)))
  }

  @Test
  fun testNoFixFileActionForNonFixableErrors() {
    eslint.installEslintForTest()
    eslint.doEditorHighlightingTestWithoutCopy("test.js")
    assertThat(eslint.fixture.filterAvailableIntentions("ESLint: Fix current file")).isEmpty()
  }

  // The following exercise eslint-plugin-html (embedded <script> linting) and JSX via a shared,
  // extension-specific flat config copied to the project root. Their configs import plugins, so they
  // install the FULL dependency set rather than the eslint-only CORE set.
  @Test
  fun testSuppressForFileInHtml(): Unit =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current file", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  @Test
  fun testSuppressMultiLinesByLineComment(): Unit =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current line", ".js", "eslint.config.html.mjs", EslintPackageSet.FULL)

  @Test
  fun testSuppressForLineInHtml(): Unit =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current line", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  @Test
  fun testFixInHtml(): Unit = doQuickFixTest("ESLint: Fix current file", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  @Test
  fun testFixWorksInJsx(): Unit = doQuickFixTest("ESLint: Fix current file", ".jsx", "eslint.config.jsx.mjs", EslintPackageSet.FULL)

  @Test
  fun testScriptsInHtmlFile() {
    installEslintWithSharedConfig("eslint.config.html.mjs", EslintPackageSet.FULL)
    eslint.fixture.configureByFile(eslint.getTestName(false) + ".html")
    // The whole-file fix action is not offered for scripts embedded in HTML.
    val fixActions = eslint.fixture.availableIntentions.filter { it.text == "ESLint: Fix current file" }
    assertThat(fixActions).isEmpty()
  }

  @Test
  fun testRunEslintFixOnSave() {
    installEslintOnly()
    val configuration = EslintConfiguration.getInstance(eslint.project)
    val eslintState = configuration.extendedState.state
    val origEnabled = configuration.isEnabled
    val origRunOnSave = eslintState.isRunOnSave
    configuration.setExtendedState(true, EslintState.Builder(eslintState).setRunOnSave(true).build())
    try {
      eslint.fixture.addFileToProject("eslint.config.js", "module.exports = {rules: {\"semi\": \"error\"}}")
      eslint.fixture.configureByText("foo.js", "var a = ''")
      eslint.fixture.type(' ')
      eslint.fixture.performEditorAction("SaveAll")
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(eslint.project)
      eslint.fixture.checkResult(" var a = '';")
    }
    finally {
      configuration.setExtendedState(origEnabled, EslintState.Builder(eslintState).setRunOnSave(origRunOnSave).build())
    }
  }

  private fun fixProblemsDescription(ruleCode: String): String =
    JavaScriptBundle.message("eslint.fix.problems.text.with.error.code",
                             EslintBundle.message("settings.javascript.linters.eslint.configurable.name"),
                             ruleCode)
}

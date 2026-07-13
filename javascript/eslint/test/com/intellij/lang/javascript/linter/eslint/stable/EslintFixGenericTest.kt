// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.linter.eslint.EslintState
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.util.LineSeparator
import com.intellij.util.ThrowableRunnable
import org.junit.Assert

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
abstract class EslintFixGenericTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/quickfix/"

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
    myFixture.copyFileToProject(packageSet.rootManifest, "package.json")
    myFixture.copyFileToProject(configSource, "eslint.config.mjs")
    installEslintFromProjectRoot()
  }

  /** Installs ESLint from the eslint-only root manifest without any flat config -- for tests that add
   *  their own config (e.g. run-on-save with an inline `eslint.config.js`). */
  protected fun installEslintOnly() {
    prepareProjectForInstall()
    myFixture.copyFileToProject(EslintPackageSet.CORE.rootManifest, "package.json")
    installEslintFromProjectRoot()
  }

  private fun prepareProjectForInstall() {
    prepareFixtureForHighlighting()
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
  protected fun doQuickFixTestForDirectory(
    description: String,
    mainFileName: String,
    extension: String,
    lineSeparator: LineSeparator? = null,
  ) {
    installEslintForTest()
    // installEslintForTest already copied the test directory; use the WithoutCopy variant so the fix run
    // does not re-copy (which would clobber the substituted package.json with the raw $ESLINT_VERSION$).
    doFixTestForDirectoryWithoutCopy(mainFileName, extension, description, ThrowableRunnable<Throwable> {
      if (lineSeparator != null) {
        JSTestUtils.ensureLineSeparators(myFixture.file, lineSeparator)
      }
    })
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

  fun testFixSingleError() =
    doQuickFixTestForDirectory(fixProblemsDescription("no-multiple-empty-lines"), "test", ".js")

  fun testFixSingleErrorWithWindowsLineSeparators() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory(fixProblemsDescription("no-multiple-empty-lines"), "test", ".js", LineSeparator.CRLF)
  }

  fun testFixFileWithWindowsLineSeparator() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.CRLF)
    FileDocumentManager.getInstance().saveAllDocuments()
    Assert.assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(myFixture.file.virtualFile)))
  }

  fun testFixFileWithConvertLineSeparators() {
    if (!SystemInfo.isWindows) return
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.LF)
    FileDocumentManager.getInstance().saveAllDocuments()
    Assert.assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(myFixture.file.virtualFile)))
  }

  fun testNoFixFileActionForNonFixableErrors() {
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy("test.js")
    assertEmpty(myFixture.filterAvailableIntentions("ESLint: Fix current file"))
  }

  // The following exercise eslint-plugin-html (embedded <script> linting) and JSX via a shared,
  // extension-specific flat config copied to the project root. Their configs import plugins, so they
  // install the FULL dependency set rather than the eslint-only CORE set.
  fun testSuppressForFileInHtml() =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current file", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  fun testSuppressMultiLinesByLineComment() =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current line", ".js", "eslint.config.html.mjs", EslintPackageSet.FULL)

  fun testSuppressForLineInHtml() =
    doQuickFixTest("Suppress 'no-multiple-empty-lines' for current line", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  fun testFixInHtml() = doQuickFixTest("ESLint: Fix current file", ".html", "eslint.config.html.mjs", EslintPackageSet.FULL)

  fun testFixWorksInJsx() = doQuickFixTest("ESLint: Fix current file", ".jsx", "eslint.config.jsx.mjs", EslintPackageSet.FULL)

  fun testScriptsInHtmlFile() {
    installEslintWithSharedConfig("eslint.config.html.mjs", EslintPackageSet.FULL)
    myFixture.configureByFile(getTestName(false) + ".html")
    // The whole-file fix action is not offered for scripts embedded in HTML.
    val fixActions = myFixture.availableIntentions.filter { it.text == "ESLint: Fix current file" }
    assertEmpty(fixActions)
  }

  fun testRunEslintFixOnSave() {
    installEslintOnly()
    val configuration = EslintConfiguration.getInstance(project)
    val eslintState = configuration.extendedState.state
    val origEnabled = configuration.isEnabled
    val origRunOnSave = eslintState.isRunOnSave
    configuration.setExtendedState(true, EslintState.Builder(eslintState).setRunOnSave(true).build())
    try {
      myFixture.addFileToProject("eslint.config.js", "module.exports = {rules: {\"semi\": \"error\"}}")
      myFixture.configureByText("foo.js", "var a = ''")
      myFixture.type(' ')
      myFixture.performEditorAction("SaveAll")
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(project)
      myFixture.checkResult(" var a = '';")
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

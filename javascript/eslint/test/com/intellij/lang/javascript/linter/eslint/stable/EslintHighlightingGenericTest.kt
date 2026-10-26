// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.linter.eslint.EslintUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.TrustedProjectsTestUtil
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.LineSeparator
import org.junit.Assert

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

  fun testMultilineError() {
    doHighlightingTestWithInstallation("test.js")
    doBatchInspectionTest()
  }

  fun testEolLastNever() {
    doHighlightingTestWithInstallation("test.js")
    doBatchInspectionTest()
  }

  fun testLineSeparatorsWin() {
    doHighlightingTestWithInstallation("test.js") {
      JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.CRLF)
    }
    doBatchInspectionTest()
  }

  // warn.js would be flagged (no-console / no-debugger), but no highlighting is expected because the project is untrusted.
  fun testNoLintingForUntrustedProject() {
    TrustedProjectsTestUtil.withTrustedProjectsCheckEnabled {
      try {
        doHighlightingTestWithInstallation("warn.js") {
          TrustedProjects.setProjectTrusted(project, false)
        }
      }
      finally {
        TrustedProjects.setProjectTrusted(project, true)
      }
    }
  }

  fun testDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, testRootDisposable)
    DumbModeTestUtils.runInDumbModeSynchronously(project) {
      doHighlightingTestWithInstallation("warn.js")
    }
    doBatchInspectionTest()
  }

  fun testMissingConfigErrorReported() {
    // eslint 10's programmatic API reports "Could not find config file." when no eslint.config.* is
    // present (was "No ESLint configuration found" on eslint 8) -- re-recorded for the 8->10 bump.
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: Error: Could not find config file.", true, false)
    doHighlightingTestWithInstallation("test.js")
  }

  // With no config file, the missing-config error is suppressed when ESLint is autodetected (the user
  // installed it but has not configured it yet). eslint8-plugin.ts recognizes both the eslint 8
  // ("No ESLint configuration found") and eslint 9/10 ("Could not find config file.") message shapes.
  fun testSuppressMissingConfigErrorWithAutodetectPackage() {
    installEslintForTestWithAutodetect()
    AutodetectLinterPackage.setTestAutodetectedPackage(project, getNodePackage(), testRootDisposable)
    doEditorHighlightingTestWithoutCopy("test.js")
  }

  // A .ts file with no config: the missing-config error is suppressed for TypeScript on every version.
  fun testSuppressMissingConfigErrorForTypescript() = doHighlightingTestWithInstallation("test.ts")

  fun testTimeout() {
    // Deterministic timeout handling without a real node service (WEB-67172): a stub eslint package (no
    // npm install -- keeps the @latest canary offline) satisfies getNodePackage(), then the analysis
    // runs against a never-responding fake service and we assert the file-level timeout annotation.
    copyTestDataToProject()
    installStubEslintPackage()
    val psiFile = myFixture.configureByFile("test.js")
    JSLanguageServiceUtil.setTimeout(1L, testRootDisposable)

    val annotation = highlightWithNeverRespondingService(psiFile).fileLevelError
    Assert.assertNotNull("Expected a file-level timeout annotation", annotation)
    val expected = JSLanguageServiceUtil.getTimeoutMessage("test.js", EslintUtil.getTimeout())
    Assert.assertTrue("Actual annotation: ${annotation?.message}", annotation?.message?.contains(expected) == true)
  }

  fun testOverrideConfigSeverityFromInspection() {
    val shortName = JSInspection.calcShortNameFromClass(EslintInspection::class.java)
    JSTestUtils.doWithChangedInspectionHighlightLevel(project, shortName, HighlightDisplayLevel.WEAK_WARNING) {
      val inspection = InspectionProfileManager.getInstance(project).currentProfile
        .getInspectionTool(shortName, project)!!.tool as EslintInspection
      inspection.useSeverityFromConfigFile = false
      doHighlightingTestWithInstallation("test.js")
    }
  }

  fun testWithRulesInOptions() {
    installEslintForTest()
    // doEditorHighlightingTest re-copies the test dir each call, restoring the inline markers that the
    // previous highlight stripped from the document -- required because this test highlights twice.
    doEditorHighlightingTest<Throwable>("test.js") {
      updateConfiguration { it.setExtraOptions("--rule 'no-console: 1'") }
    }
    FileDocumentManager.getInstance().saveAllDocuments()
    doEditorHighlightingTest<Throwable>("test.js") {
      updateConfiguration { it.setExtraOptions("--rule \"no-console: 'warn'\"") }
    }
  }

  // Parse error (flat config with sourceType:script + ecmaVersion:5) reported as a fatal lint error;
  // also exercises the fatal Annotation -> ProblemDescriptor batch mapping. Parse-error text/shape is a
  // volatile eslint surface, so keeping these in the generic tier lets the @latest canary catch changes.
  fun testReportAboutWrongParser() {
    doHighlightingTestWithInstallation("test.js")
    doBatchInspectionTest()
  }

  fun testESLintLocalFatalError() {
    doHighlightingTestWithInstallation("test.js")
    doBatchInspectionTest()
  }

  // A flat config that imports a nonexistent module fails to load -> config-load fatal, surfaced as a
  // file-level ESLint error (asserts a stable fragment, not the exact node/eslint message).
  fun testESLintGlobalFatalError() {
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("eslint-config-does-not-exist", false, true)
    doHighlightingTestWithInstallation("test.jsx")
  }

  // A rule that catches its own failure and only logs it to stderr (WEB-78300): the IDE shows the logged exception as a
  // file-level ESLint error, and the problems that the other rules report stay. The stack frames hold temp paths, so
  // the check stops at the first frame.
  fun testStderrOutputReported() {
    val loggedError = "Could not read file [object Object]\nError: ENOENT: no such file or directory\n    at "
    val message = EslintBundle.message("eslint.inspections.error.logged.error", loggedError)
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: $message", true, false)
    doHighlightingTestWithInstallation("test.js")
  }

  // A rule that ends the service process, as a crash of the runtime does (WEB-78300): the request fails at once with
  // the exit code instead of a wait for the timeout, and the file-level error is set before the service restarts.
  // The last stderr lines, where a runtime prints its crash report, are part of the error without color codes.
  fun testServiceProcessExitReported() {
    val message = EslintBundle.message("eslint.service.process.exited", 3, "").trimEnd()
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: $message", true, false)
    doHighlightingTestWithInstallation("test.js")
    val text = getAnnotationText()!!
    Assert.assertTrue(text, text.contains("panic: the runtime failed"))
    Assert.assertFalse(text, text.contains("\u001B"))
  }

  // A process that exits before it is ready is a start failure, not an unexpected exit: the start error stays.
  fun testServiceStartFailureKeepsItsError() {
    Registry.get("eslint.service.node.arguments").setValue("--not-an-option", testRootDisposable)
    val message = JavaScriptBundle.message("javascript.language.service.cannot.start")
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: $message", true, false)
    doHighlightingTestWithInstallation("test.js")
  }

  // A rule that logs a warning without a stack, as eslint-plugin-react does for a missing React version: no error mark,
  // and the problems that the other rules report stay.
  fun testStderrWarningNotReported() = doHighlightingTestWithInstallation("test.js")

  // A rule that prints a deprecation the way Node.js does under --trace-deprecation, with a stack: a process warning of
  // the runtime is not a failure of a rule, so no error mark.
  fun testStderrRuntimeWarningNotReported() = doHighlightingTestWithInstallation("test.js")

  // A rule that logs an exception and then 70000 characters: the capture keeps the first 64 KiB, and the error ends
  // with the note about the cap.
  fun testStderrOutputTruncated() {
    val note = EslintBundle.message("eslint.service.stderr.truncated")
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation(note, false, true)
    doHighlightingTestWithInstallation("test.js")
    val text = getAnnotationText()!!
    val header = EslintBundle.message("eslint.inspections.error.logged.error", "")
    Assert.assertTrue(text.take(200), text.startsWith("ESLint: $header"))
    Assert.assertTrue(text.length.toString(), text.length < 70_000)
  }

  // A file matched by flat-config `ignores` is skipped (isPathIgnored), so no problems are reported.
  fun testFileIgnored() {
    doHighlightingTestWithInstallation("testIgnored.js")
    doBatchInspectionTest()
  }

  // Bulk suppressions (WEB-74146): a violation recorded in eslint-suppressions.json is not reported, the other one is.
  fun testBulkSuppressions() {
    doHighlightingTestWithInstallation("index.js")
    doBatchInspectionTest()
  }

  // More violations of a rule than the suppressions file records: every violation of that rule is reported, as in the CLI.
  fun testBulkSuppressionsCountExceeded() = doHighlightingTestWithInstallation("index.js")

  // The suppressions file at the `--suppressions-location` path from the extra options is applied.
  fun testBulkSuppressionsCustomLocation() = doHighlightingTestWithInstallation("index.js") {
    updateConfiguration { it.setExtraOptions("--suppressions-location custom-suppressions.json") }
  }
}

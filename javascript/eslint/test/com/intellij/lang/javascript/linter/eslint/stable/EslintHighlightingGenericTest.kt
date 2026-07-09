// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.linter.eslint.EslintUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.testFramework.DumbModeTestUtils
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
    try {
      doHighlightingTestWithInstallation("warn.js") {
        TrustedProjects.setProjectTrusted(project, false)
      }
    }
    finally {
      TrustedProjects.setProjectTrusted(project, true)
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

  fun testTimeout() {
    // Deterministic timeout handling without a real node service (WEB-67172): install eslint, point the
    // linter at it, then run the analysis against a never-responding fake service and assert the
    // file-level timeout annotation.
    installEslintForTest()
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
}

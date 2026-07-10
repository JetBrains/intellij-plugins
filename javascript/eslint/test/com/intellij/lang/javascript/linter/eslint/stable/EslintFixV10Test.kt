// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.inspections.JSConsecutiveCommasInArrayLiteralInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.linter.eslint.ESLINT_10_6_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.modules.TestNpmPackage

/**
 * The primary pinned ESLint quick-fix suite: flat config on eslint 10.6.0. Runs every generic
 * scenario plus the version-specific fixes below -- their intention-order assertions depend on
 * eslint 10's exact suggestion set/ordering, so they must NOT run on the version-agnostic tier.
 * A failure here is a product regression.
 */
@TestNpmPackage(ESLINT_10_6_0_TEST_PACKAGE_SPEC)
class EslintFixV10Test : EslintFixGenericTest() {
  fun testSuppressQuickFixGoesAfterInspectionFix() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy("test.js")

    val quickFixNames = myFixture.availableIntentions.map { it.text }
    assertContainsOrdered(quickFixNames,
                          "Remove unused variable 'foo'",
                          "Edit inspection profile setting",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress for file",
                          "Suppress for statement",
                          // eslint 10's no-unused-vars provides a "Remove unused variable" suggestion
                          // (eslint 8 had none), so this ESLint fix now heads the ESLint action group.
                          "ESLint: Remove unused variable 'foo'.",
                          "Edit inspection profile setting",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress 'no-unused-vars' for current file",
                          "Suppress all ESLint rules for current file",
                          "Suppress 'no-unused-vars' for current line",
                          "Suppress all ESLint rules for current line")
  }

  fun testEsLintQuickFixGoesAfterInspectionFix() {
    myFixture.enableInspections(JSConsecutiveCommasInArrayLiteralInspection::class.java)
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy("test.js")

    val quickFixNames = myFixture.availableIntentions.map { it.text }
    assertContainsOrdered(quickFixNames,
                          "Insert 'undefined'",
                          "Edit inspection profile setting",
                          "Fix all 'Consecutive commas in array literal' problems in file",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress for file",
                          "Suppress for statement",
                          "Remove unneeded comma",
                          "Edit inspection profile setting",
                          "Fix all 'Consecutive commas in array literal' problems in file",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress for file",
                          "Suppress for statement",
                          "ESLint: Fix 'comma-spacing'",
                          "Edit inspection profile setting",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress 'comma-spacing' for current file",
                          "Suppress all ESLint rules for current file",
                          "Suppress 'comma-spacing' for current line",
                          "Suppress all ESLint rules for current line",
                          "ESLint: Fix current file",
                          "Edit inspection profile setting",
                          "Run inspection on…",
                          "Disable highlighting, keep fix",
                          "Disable inspection",
                          "Suppress 'comma-spacing' for current file",
                          "Suppress all ESLint rules for current file",
                          "Suppress 'comma-spacing' for current line",
                          "Suppress all ESLint rules for current line",
                          "Flip ',' (may change semantics)",
                          "Edit intention settings",
                          "Disable 'Flip comma'",
                          "Assign shortcut…",
                          "Split into declaration and initialization",
                          "Edit intention settings",
                          "Disable 'Split declaration and initialization'",
                          "Assign shortcut…",
                          "Disable option 'Variables and fields' for 'Type annotations' inlay hints",
                          "Put comma-separated elements on multiple lines",
                          "Edit intention settings",
                          "Disable 'Put elements on multiple lines'",
                          "Assign shortcut…")
  }
}

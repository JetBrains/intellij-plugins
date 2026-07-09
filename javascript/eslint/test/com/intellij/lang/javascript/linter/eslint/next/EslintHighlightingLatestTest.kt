// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.next

import com.intellij.lang.javascript.linter.eslint.ESLINT_LATEST_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.stable.EslintHighlightingGenericTest
import com.intellij.lang.javascript.modules.TestNpmPackage

/**
 * The version-agnostic highlighting scenarios run against `eslint@latest`. These MAY fail when ESLint
 * ships a breaking change — that is their purpose. A failure here means "adapt the plugin / re-pin the
 * stable suite and re-record goldens", NOT a regression in whatever change happens to be in the merge
 * queue. (The lock-store installer exempts `@latest` from the strict stored-lock requirement, so this
 * tier resolves the newest ESLint from the registry.)
 */
@TestNpmPackage(ESLINT_LATEST_TEST_PACKAGE_SPEC)
class EslintHighlightingLatestTest : EslintHighlightingGenericTest()

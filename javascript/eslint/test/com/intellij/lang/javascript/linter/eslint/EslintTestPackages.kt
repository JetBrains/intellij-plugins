// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint

/**
 * ESLint version constants for the PackageLock-based tests.
 *
 * If you change a version number here, regenerate the stored lock files for the corresponding
 * version in:
 *   contrib/javascript/eslint/testData/linter/eslint/highlighting/_package-locks-store
 *   contrib/javascript/eslint/testData/linter/eslint/quickfix/_package-locks-store
 * (see the "Upgrade procedure" in the store's README.md), then rerun the `stable` classes and
 * re-record any goldens that legitimately changed.
 */
const val ESLINT_10_6_0_TEST_PACKAGE_SPEC: String = "eslint@10.6.0"

/** Legacy `.eslintrc` coverage only — see EslintHighlightingV8LegacyTest. */
const val ESLINT_8_57_0_TEST_PACKAGE_SPEC: String = "eslint@8.57.0"

/**
 * eslint 9 one-off — the only major that supports BOTH flat config (default) and, via
 * `LegacyESLint` from `../lib/unsupported-api`, eslintrc fallback. See EslintFallbackToLegacyConfigV9Test.
 */
const val ESLINT_9_11_1_TEST_PACKAGE_SPEC: String = "eslint@9.11.1"

// next versions
const val ESLINT_LATEST_TEST_PACKAGE_SPEC: String = "eslint@latest"

/**
 * Placeholder used in testData `package.json` files. Replaced at runtime with the version from the
 * class-level [com.intellij.lang.javascript.modules.TestNpmPackage] annotation, so a single testData
 * tree serves every pinned version.
 */
const val ESLINT_VERSION_PLACEHOLDER: String = "\$ESLINT_VERSION\$"

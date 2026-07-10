// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_10_6_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.modules.TestNpmPackage

/**
 * The primary pinned ESLint quick-fix suite: flat config on eslint 10.6.0. Runs every generic
 * scenario plus any flat-config-specific fixes. A failure here is a product regression.
 */
@TestNpmPackage(ESLINT_10_6_0_TEST_PACKAGE_SPEC)
class EslintFixV10Test : EslintFixGenericTest()

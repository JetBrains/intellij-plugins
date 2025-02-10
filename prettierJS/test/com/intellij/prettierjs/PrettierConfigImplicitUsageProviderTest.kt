// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase


class PrettierConfigImplicitUsageProviderTest: BasePlatformTestCase() {

  override fun getTestDataPath(): String = "${PrettierJSTestUtil.getTestDataPath()}/implicitUsages"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
  }

  fun testConfigImplicitUsage() {
    myFixture.testHighlighting(true, false, true, ".prettierrc.mjs", ".prettier.js", ".prettier.ts")
  }

  fun testFalsePositiveImplicitUsage() {
    myFixture.testHighlighting(true, false, true, "prettier.js")
  }
}
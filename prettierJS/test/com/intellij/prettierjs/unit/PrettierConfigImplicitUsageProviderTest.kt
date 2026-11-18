// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@TestApplication
class PrettierConfigImplicitUsageProviderTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val myFixture by sharedCodeInsightFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    myFixture.testDataPath = "${PrettierJSTestUtil.getTestDataPath()}/implicitUsages"
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
  }

  @Test
  fun testConfigImplicitUsage() {
    myFixture.testHighlighting(true, false, true, ".prettierrc.mjs", ".prettier.js", ".prettier.ts")
  }

  @Test
  fun testFalsePositiveImplicitUsage() {
    myFixture.testHighlighting(true, false, true, "prettier.js")
  }
}
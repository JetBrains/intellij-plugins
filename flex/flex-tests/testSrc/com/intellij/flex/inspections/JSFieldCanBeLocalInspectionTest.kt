// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections

import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.inspections.actionscript.JSFieldCanBeLocalInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JSFieldCanBeLocalInspectionTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSFieldCanBeLocalInspection())
  }

  override fun getTestDataPath(): String {
    return FlexTestUtils.getTestDataPath("/global_inspections/JSFieldCanBeLocal")
  }

  fun testMain() {
    myFixture.testHighlighting(true, false, true, getTestName(false) + ".as")
  }
}
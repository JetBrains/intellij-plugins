// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections

import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.inspections.JSMismatchedCollectionQueryUpdateInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JSMismatchedCollectionQueryUpdateInspectionTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSMismatchedCollectionQueryUpdateInspection())
  }

  override fun getTestDataPath(): String {
    return FlexTestUtils.getTestDataPath("/global_inspections/JSMismatchedCollectionQueryUpdate")
  }

  fun testBasicActionScript() {
    myFixture.testHighlighting(true, false, false, "basicActionScript.as")
  }
}

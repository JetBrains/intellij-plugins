// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections

import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sixrr.inspectjs.confusing.PointlessBooleanExpressionJSInspection

class JSPointlessBooleanExpressionInspectionTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(PointlessBooleanExpressionJSInspection())
  }

  override fun getTestDataPath(): String {
    return FlexTestUtils.getTestDataPath("/global_inspections/PointlessBooleanExpression")
  }

  fun testSimplifyAS() {
    myFixture.configureByFile("simplifyAS.js2")
    myFixture.checkHighlighting()
    myFixture.launchAction(myFixture.findSingleIntention(JavaScriptBundle.message("simplify.fix")))
    myFixture.checkResultByFile("simplifyAS_after.js2")
  }
}

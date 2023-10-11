// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.javascript.web.WebFrameworkTestModule
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile

class Angular2QuickFixesTest : Angular2TestCase("inspections/quickFixes") {

  fun testBooleanTransformAttr() =
    doTest("Use booleanAttribute", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testBooleanTransformBinding() =
    doTest("Use booleanAttribute", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testNumberTransformAttr() =
    doTest("Use numberAttribute", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testNumberTransformBinding() =
    doTest("Use numberAttribute", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testCustomTransformAttr() =
    doTest("Create input transformer", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testCustomTransformBinding() =
    doTest("Create input transformer", Angular2TestModule.ANGULAR_CORE_16_2_8)

  fun testCreateSignalFromUsage() =
    doTest("Create signal", Angular2TestModule.ANGULAR_CORE_16_2_8)

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  private fun doTest(intentionName: String, vararg modules: WebFrameworkTestModule, dir: Boolean = false) {
    doConfiguredTest(*modules, dir = dir, checkResult = true, configurators = listOf(Angular2TsConfigFile())) {
      myFixture.launchAction(myFixture.findSingleIntention(intentionName))
    }
  }

}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_COMMON_16_0_0_NEXT_4

class Angular2HighlightingTest: Angular2TestCase("highlighting") {

  fun testSvgTags() = checkHighlighting(ANGULAR_COMMON_16_0_0_NEXT_4)

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  private fun checkHighlighting(vararg modules: Angular2TestModule) {
    doConfiguredTest(*modules) {
      checkHighlighting()
    }
  }

}
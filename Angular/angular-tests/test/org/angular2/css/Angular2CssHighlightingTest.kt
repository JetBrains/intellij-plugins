// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2CssHighlightingTest: Angular2TestCase("css/highlighting", false) {

  // WEB-63400
  fun testCssAmpersandSelector() = checkHighlighting()

  // WEB-63587
  fun testLessParentSelector() = checkHighlighting(extension = "less")

  // WEB-63587
  fun testPcssAmpersand() = checkHighlighting(extension = "pcss")

  fun testHostBindingClassBindingUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingClassAttributeUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  fun testHostBindingDecoratorClassUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(CssUnusedSymbolInspection())
  }

  private fun checkHighlighting(
    vararg modules: Angular2TestModule,
    extension: String = "css"
  ) {
    checkHighlighting(*modules, configureFileName = "$testName.$extension")
  }
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.angular2.TestTsKotlin
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedMixinInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
import org.junit.Test

@TestNoService
@TestTsGoProxy
@TestTsKotlin
class Angular2CssHighlightingTest : Angular2TestCase("css/highlighting") {

  // WEB-63400
  @Test
  fun testCssAmpersandSelector() = checkHighlighting()

  // WEB-63587
  @Test
  fun testLessParentSelector() = checkHighlighting(extension = "less")

  // WEB-63587
  @Test
  fun testPcssAmpersand() = checkHighlighting(extension = "pcss")

  @Test
  fun testHostBindingClassBindingUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingClassAttributeUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingDecoratorClassUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  // WEB-79721
  @Test
  fun testScssUseNodeModulesInlineStyles() = checkInlineStylesImports()

  // WEB-56888
  @Test
  fun testScssTildeImportInlineStyles() = checkInlineStylesImports()

  // WEB-56888
  @Test
  fun testLessTildeImportInlineStyles() = checkInlineStylesImports()

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(CssUnusedSymbolInspection())
  }

  private fun checkHighlighting(
    vararg modules: Angular2TestModule,
    extension: String = "css",
  ) {
    doHighlightingTest(*modules, configureFileName = "$testName.$extension")
  }

  private fun checkInlineStylesImports() {
    doHighlightingTest(dir = true, configureFileName = "src/app/app.component.ts",
                       inspections = listOf(CssUnknownTargetInspection::class.java, SassScssUnresolvedMixinInspection::class.java,
                                            SassScssUnresolvedVariableInspection::class.java))
  }
}

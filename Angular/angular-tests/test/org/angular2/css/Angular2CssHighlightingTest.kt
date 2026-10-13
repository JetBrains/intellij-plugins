// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedMixinInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2CssHighlightingTest : Angular2TestCase("css/highlighting") {

  @Test
  fun testCssAmpersandSelector() = checkHighlighting()

  @Test
  fun testLessParentSelector() = checkHighlighting(extension = "less")

  @Test
  fun testPcssAmpersand() = checkHighlighting(extension = "pcss")

  @Test
  fun testHostBindingClassBindingUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingClassAttributeUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testHostBindingDecoratorClassUnused() = checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "ts")

  @Test
  fun testScssUseNodeModulesInlineStyles() = checkInlineStylesImports()

  @Test
  fun testScssTildeImportInlineStyles() = checkInlineStylesImports()

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

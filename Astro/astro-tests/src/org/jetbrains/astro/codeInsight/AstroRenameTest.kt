package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Test

@TestApplication
class AstroRenameTest : AstroCodeInsightTestCase("codeInsight/refactoring/rename") {
  @Test
  fun testAstroComponentProp() = astro.doSymbolRenameTest("astroComponentProp.astro", "renamedProp")

  @Test
  fun testAstroComponentProp2() = astro.doSymbolRenameTest("component.astro", "renamedProp")

  @Test
  fun testReactNamespacedComponent() = astro.doSymbolRenameTest("index.astro", "NewComponent")
}
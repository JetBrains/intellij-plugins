package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroRenameTest : AstroCodeInsightTestCase("codeInsight/refactoring/rename") {
  fun testAstroComponentProp() = doSymbolRenameTest("astroComponentProp.astro", "renamedProp")

  fun testAstroComponentProp2() = doSymbolRenameTest("component.astro", "renamedProp")

  fun testReactNamespacedComponent() = doSymbolRenameTest("index.astro", "NewComponent")
}
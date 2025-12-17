package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroRenameTest : AstroCodeInsightTestCase("codeInsight/refactoring/rename") {
  fun testAstroComponentProp() = checkSymbolRename("astroComponentProp.astro", "renamedProp")

  fun testAstroComponentProp2() = checkSymbolRename("component.astro", "renamedProp")

  fun testReactNamespacedComponent() = checkSymbolRename("index.astro", "NewComponent")
}
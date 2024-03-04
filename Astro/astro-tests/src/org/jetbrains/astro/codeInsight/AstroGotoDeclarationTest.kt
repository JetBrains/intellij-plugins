package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroGotoDeclarationTest : AstroCodeInsightTestCase("codeInsight/navigation/declaration") {
  fun testAstroComponentProp() = checkGotoDeclaration("<caret>title: string,", expectedFileName = "component.astro", dir = true)
}
package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroFindUsagesTest : AstroCodeInsightTestCase("codeInsight/navigation/findUsages") {
  fun testAstroComponentProp() = checkUsages()
}
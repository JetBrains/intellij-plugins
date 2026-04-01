package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroFindUsagesTest : AstroCodeInsightTestCase("codeInsight/navigation/findUsages") {
  fun testAstroComponentProp() = doFindUsagesTest()

  fun testComponentFile() = doFileUsagesTest(fileName = "src/components/temp/ManyImportsUpd.astro")
}
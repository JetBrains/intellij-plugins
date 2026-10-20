package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Test

@TestApplication
class AstroFindUsagesTest : AstroCodeInsightTestCase("codeInsight/navigation/findUsages") {
  @Test
  fun testAstroComponentProp() = astro.doFindUsagesTest()

  @Test
  fun testComponentFile() = astro.doFileUsagesTest(fileName = "src/components/temp/ManyImportsUpd.astro")
}
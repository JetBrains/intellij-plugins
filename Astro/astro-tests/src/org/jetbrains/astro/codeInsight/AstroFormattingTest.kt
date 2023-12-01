package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroFormattingTest : AstroCodeInsightTestCase("codeInsight/formatting") {

  fun testBasic() = doFormattingTest()

  fun testWhitespacesBeforeFrontmatter() = doFormattingTest()

  fun testScriptTag() = doFormattingTest()

}
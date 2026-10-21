package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Test

@TestApplication
class AstroFormattingTest {

  private val astro: AstroFixture by astroFixture("codeInsight/formatting")

  @Test
  fun testBasic() = astro.doFormattingTest()

  @Test
  fun testWhitespacesBeforeFrontmatter() = astro.doFormattingTest()

  @Test
  fun testScriptTag() = astro.doFormattingTest()

}
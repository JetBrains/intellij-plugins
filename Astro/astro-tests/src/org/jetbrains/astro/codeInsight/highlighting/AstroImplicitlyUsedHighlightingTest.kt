package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test


@TestApplication
class AstroImplicitlyUsedHighlightingTest {

  private val astro: AstroHighlightingFixture by astroHighlightingFixture("codeInsight/highlighting/implicitlyUsed")

  @Test
  fun testGetStaticPaths() = astro.doTest()
  @Test
  fun testGetStaticPathsVariableDeclaration() = astro.doTest()
  @Test
  fun testPrerender() = astro.doTest()
  @Test
  fun testPartial() = astro.doTest()
  @Test
  fun testPropsTypeAlias() = astro.doTest()
}
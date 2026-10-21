package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test


@TestApplication
class AstroSuppressedInspectionsHighlightingTest {

  private val astro: AstroHighlightingFixture by astroHighlightingFixture("codeInsight/highlighting/suppressedInspections")

  @Test
  fun testUnusedImport() = astro.doTest()
  @Test
  fun testUnknownAttribute() = astro.doTest()
  @Test
  fun testValidateAttributeValueThroughPolySymbols() = astro.doTest()
  @Test
  fun testNotBoundedNamespace() = astro.doTest()
}
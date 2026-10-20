package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test


@TestApplication
class AstroSuppressedInspectionsHighlightingTest : AstroHighlightingTestBase("codeInsight/highlighting/suppressedInspections") {
  @Test
  fun testUnusedImport() = doTest()
  @Test
  fun testUnknownAttribute() = doTest()
  @Test
  fun testValidateAttributeValueThroughPolySymbols() = doTest()
  @Test
  fun testNotBoundedNamespace() = doTest()
}
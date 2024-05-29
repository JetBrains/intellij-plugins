package org.jetbrains.astro.codeInsight.highlighting


class AstroSuppressedInspectionsHighlightingTest : AstroHighlightingTestBase("codeInsight/highlighting/suppressedInspections") {
  fun testUnusedImport() = doTest()
}
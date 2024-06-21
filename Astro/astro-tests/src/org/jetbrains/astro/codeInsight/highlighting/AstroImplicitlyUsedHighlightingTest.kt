package org.jetbrains.astro.codeInsight.highlighting


class AstroImplicitlyUsedHighlightingTest : AstroHighlightingTestBase("codeInsight/highlighting/implicitlyUsed") {
  fun testGetStaticPaths() = doTest()
  fun testGetStaticPathsVariableDeclaration() = doTest()
}
package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test


@TestApplication
class AstroImplicitlyUsedHighlightingTest : AstroHighlightingTestBase("codeInsight/highlighting/implicitlyUsed") {
  @Test
  fun testGetStaticPaths() = doTest()
  @Test
  fun testGetStaticPathsVariableDeclaration() = doTest()
  @Test
  fun testPrerender() = doTest()
  @Test
  fun testPartial() = doTest()
  @Test
  fun testPropsTypeAlias() = doTest()
}
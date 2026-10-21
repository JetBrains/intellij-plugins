package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.junit.jupiter.api.Test

@TestApplication
class AstroRenameTest {

  private val astro: AstroFixture by astroFixture("codeInsight/refactoring/rename")

  @Test
  fun testAstroComponentProp() = astro.doSymbolRenameTest("astroComponentProp.astro", "renamedProp")

  @Test
  fun testAstroComponentProp2() = astro.doSymbolRenameTest("component.astro", "renamedProp")

  @Test
  fun testReactNamespacedComponent() = astro.doSymbolRenameTest("index.astro", "NewComponent")
}
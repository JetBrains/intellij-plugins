package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroHighlightingTest : AstroCodeInsightTestCase("codeInsight/highlighting") {

  fun testCharEntityResolution() = doTest()

  fun testUnusedComponentImports() = doTest(additionalFiles = listOf("component.astro"))

  fun testClientDirectives() = doTest(additionalFiles = listOf("component.astro"))

  fun testUnusedImportFalsePositive() = doTest()

  fun testImplicitConfigUsage() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    ASTRO_CONFIG_FILES.forEach {
      myFixture.addFileToProject(it, """
        import { defineConfig } from 'astro/config'
  
        // https://astro.build/config
        export default defineConfig({})
      """.trimIndent())
      myFixture.testHighlighting(it)
    }
  }

  //region Test configuration and helper methods

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(AstroInspectionsProvider())
  }

  private fun doTest(additionalFiles: List<String> = emptyList()) {
    doConfiguredTest(additionalFiles = additionalFiles) {
      checkHighlighting()
    }
  }

  //endregion
}
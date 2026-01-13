package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import org.jetbrains.astro.codeInsight.ASTRO_CONFIG_FILES

class AstroHighlightingTest : AstroHighlightingTestBase("codeInsight/highlighting") {

  fun testCharEntityResolution() = doTest()

  fun testClientDirectives() = doTest(additionalFiles = listOf("react-component.tsx"))

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

  fun testPagesPathResolving() {
    myFixture.enableInspections(HtmlUnknownTargetInspection())
    val dir = getTestName(true)
    myFixture.copyDirectoryToProject(dir, "")
    myFixture.testHighlighting(true, false, true, "src/usage/usage.astro")
  }

  fun testSlotElement() = doTest()
}
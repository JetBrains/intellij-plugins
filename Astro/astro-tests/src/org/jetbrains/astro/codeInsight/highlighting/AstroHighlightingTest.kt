package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
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

  fun testShorthandAttributeRequired() {
    myFixture.configureByText("test.astro", """
      ---
      const src = 'myImage.png';
      const alt = 'placeholder';
      ---
      <img {src} {alt}>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testImportFromFrontmatterInAstro() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection())
    myFixture.addFileToProject("Helper.astro", """
      ---
      export interface TestInterface {
        foo: string;
      }

      export function utilFunction(): string {
        return 'Hello';
      }

      export class Inner {
        foo = true;
      }
      ---
      <div>Helper</div>
    """.trimIndent())

    myFixture.configureByText("Usage.astro", """
      ---
      import { TestInterface, utilFunction, Inner } from './Helper.astro';

      const value: TestInterface = { foo: 'test' };
      const result = utilFunction();
      new Inner();
      ---
      <div>{result + value}</div>
    """.trimIndent())
    myFixture.testHighlighting()
  }

  fun testImportFromFrontmatterInTs() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection())
    myFixture.addFileToProject("Helper.astro", """
      ---
      export interface TestInterface {
        foo: string;
      }

      export function utilFunction(value: TestInterface): string {
        return 'Hello';
      }
      ---
      <div>Component</div>
    """.trimIndent())

    myFixture.configureByText("usage.ts", """
      import { TestInterface, utilFunction} from './Helper.astro';

      const value: TestInterface = { foo: 'test' };
      utilFunction(value);
    """.trimIndent())
    myFixture.testHighlighting()
  }
}
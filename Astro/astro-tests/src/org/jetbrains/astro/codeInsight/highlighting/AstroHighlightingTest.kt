package org.jetbrains.astro.codeInsight.highlighting

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.codeInsight.ASTRO_CONFIG_FILES
import org.junit.jupiter.api.Test

@TestApplication
class AstroHighlightingTest {

  private val astro: AstroHighlightingFixture by astroHighlightingFixture("codeInsight/highlighting")

  @Test
  fun testCharEntityResolution() = astro.doTest()

  @Test
  fun testClientDirectives() = astro.doTest(additionalFiles = listOf("react-component.tsx"))

  @Test
  fun testImplicitConfigUsage() {
    astro.fixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    ASTRO_CONFIG_FILES.forEach {
      astro.fixture.addFileToProject(it, """
        import { defineConfig } from 'astro/config'
  
        // https://astro.build/config
        export default defineConfig({})
      """.trimIndent())
      astro.fixture.testHighlighting(it)
    }
  }

  @Test
  fun testPagesPathResolving() {
    astro.fixture.enableInspections(HtmlUnknownTargetInspection())
    val dir = astro.getTestName(true)
    astro.fixture.copyDirectoryToProject(dir, "")
    astro.fixture.testHighlighting(true, false, true, "src/usage/usage.astro")
  }

  @Test
  fun testSlotElement() = astro.doTest()

  @Test
  fun testShorthandAttributeRequired() {
    astro.fixture.configureByText("test.astro", """
      ---
      const src = 'myImage.png';
      const alt = 'placeholder';
      ---
      <img {src} {alt}>
    """.trimIndent())
    astro.fixture.testHighlighting()
  }

  @Test
  fun testImportFromFrontmatterInAstro() {
    astro.fixture.enableInspections(TypeScriptUnresolvedReferenceInspection())
    astro.fixture.addFileToProject("Helper.astro", """
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

    astro.fixture.configureByText("Usage.astro", """
      ---
      import { TestInterface, utilFunction, Inner } from './Helper.astro';

      const value: TestInterface = { foo: 'test' };
      const result = utilFunction();
      new Inner();
      ---
      <div>{result + value}</div>
    """.trimIndent())
    astro.fixture.testHighlighting()
  }

  @Test
  fun testImportFromFrontmatterInTs() {
    astro.fixture.enableInspections(TypeScriptUnresolvedReferenceInspection())
    astro.fixture.addFileToProject("Helper.astro", """
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

    astro.fixture.configureByText("usage.ts", """
      import { TestInterface, utilFunction} from './Helper.astro';

      const value: TestInterface = { foo: 'test' };
      utilFunction(value);
    """.trimIndent())
    astro.fixture.testHighlighting()
  }
}
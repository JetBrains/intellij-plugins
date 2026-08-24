package org.intellij.plugin.mdx

import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSXUnresolvedComponentInspection
import com.intellij.lang.typescript.inspections.TypeScriptJSXUnresolvedComponentInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/autoImport")
class MdxAutoImportTest : MdxTestBase() {

  @BeforeEach
  fun enableImportInspections() {
    myFixture.enableInspections(
      JSXUnresolvedComponentInspection(),
      TypeScriptJSXUnresolvedComponentInspection(),
      JSUnresolvedReferenceInspection(),
      TypeScriptUnresolvedReferenceInspection()
    )
  }

  private fun doAutoImportTest(vararg companions: String = arrayOf("MyComponent.mdx")) {
    companions.forEach { myFixture.configureByFile(it) }
    myFixture.configureByFile("$testName.mdx")
    myFixture.doHighlighting()
    val intentions = myFixture.getAvailableIntentions()
    val intention = intentions.firstOrNull {
      it.text.startsWith("Insert")
    } ?: error("Auto-import quick fix should be available. Available intentions: ${intentions.map { it.text }}")
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  @Test
  fun testInsertImportFromMdxFile() {
    doAutoImportTest()
  }

  @Test
  fun testInsertImportFromTsxComponent() {
    doAutoImportTest("InsertImportFromTsxComponent_1.tsx")
  }

  @Test
  fun testInsertImportFromTsUtility() {
    doAutoImportTest("InsertImportFromTsUtility_1.ts")
  }

  @Test
  fun testInsertImportPreservesExistingImports() {
    doAutoImportTest("CompA.mdx", "CompB.mdx")
  }

  // --- Front matter: import must land AFTER the closing delimiter, never before it ---

  @Test
  fun testInsertImportAfterYamlFrontMatter() {
    // YAML front matter starts with '---' on line 1; inserting an import before it would break the file
    doAutoImportTest()
  }

  @Test
  fun testInsertImportAfterTomlFrontMatter() {
    // TOML front matter starts with '+++' on line 1; same constraint applies
    doAutoImportTest("InsertImportAfterTomlFrontMatter_1.mdx")
  }

  @Test
  fun testInsertImportWithFrontMatterAndExistingImport() {
    // Front matter + an existing import: new import should still go after front matter
    doAutoImportTest("CompA.mdx", "CompB.mdx")
  }

  @Test
  fun testInsertImportAfterYamlFrontMatterNoBlankLine() {
    // Zero-gap edge case: body starts on the very next line after the closing delimiter
    doAutoImportTest()
  }

  @Test
  fun testInsertImportGroupsWithExistingImportNoBlankLine() {
    // Existing import directly abuts the closing delimiter (no blank-line gap) - must still group
    doAutoImportTest("CompA.mdx", "CompB.mdx")
  }

  @Test
  fun testInsertImportGroupsWithExistingImportAfterSeveralBlankLines() {
    // Several blank lines before an existing import block - must still skip them all and group
    doAutoImportTest("CompA.mdx", "CompB.mdx")
  }

  @Test
  fun testInsertImportAfterYamlFrontMatterWithDotsClosingDelimiter() {
    // YAML also allows '...' as the closing delimiter, not just '---'
    doAutoImportTest()
  }

  @Test
  fun testInsertImportAfterYamlFrontMatterWithNestedYamlContent() {
    // Nested YAML lists/maps in the header must not confuse the closing-delimiter search
    doAutoImportTest()
  }

  @Test
  fun testInsertImportAfterYamlFrontMatterWithNestedJsxReference() {
    // The unresolved reference is nested inside another JSX element, not at the body's top level
    doAutoImportTest()
  }

  @Test
  fun testInsertImportAfterYamlFrontMatterWithExportStatement() {
    // A leading `export` is not an import block to group with; the import still lands right after the
    // delimiter, ahead of the blank line and the export statement
    doAutoImportTest()
  }
}
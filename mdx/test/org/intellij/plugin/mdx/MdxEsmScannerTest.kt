package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxEsmScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxEsmScannerTest {
  @Test
  fun multilineImportTerminatesOnlyAfterModuleSpecifier() {
    val prefixes = listOf(
      "import",
      "import\n{hello}",
      "import\n{hello}\nfrom",
    )
    for (prefix in prefixes) {
      assertFalse(MdxEsmScanner.scanBlock(prefix, 0)?.terminated == true, prefix)
    }

    val complete = "import\n{hello}\nfrom\n'./hello.mdx'"
    val block = MdxEsmScanner.scanBlock(complete, 0)
    assertTrue(block?.terminated == true)
    assertEquals(complete.length, block?.range?.last)
  }

  @Test
  fun incompleteImportRecoversAtBlankLine() {
    val statement = "import Widget"
    val block = MdxEsmScanner.scanBlock("$statement\n\nParagraph", 0)

    assertFalse(block?.terminated == true)
    assertTrue(block?.recoveryBoundary == true)
    assertEquals(statement.length, block?.range?.last)
  }

  @Test
  fun consecutiveSameLineImportsShareStableBlock() {
    val text = "import {unused} from 'one';import {used} from 'two';"
    val block = MdxEsmScanner.scanBlock(text, 0)

    assertTrue(block?.terminated == true)
    assertEquals(text.length, block?.range?.last)
    assertTrue(MdxEsmScanner.findMissingStatementSeparators(text, 0, text.length).isEmpty())
  }

  @Test
  fun findsMissingStatementSeparatorAtTopLevel() {
    val text = "import First from 'first'import Second from 'second'"

    assertEquals(
      listOf(text.lastIndexOf("import")),
      MdxEsmScanner.findMissingStatementSeparators(text, 0, text.length),
    )
  }

  @Test
  fun ignoresEsmKeywordsOwnedByJavaScriptTokens() {
    val statements = listOf(
      "export const value = \"import\"",
      "export const value = `export`",
      "export const value = /import/",
      "export const value = <span>export</span>",
      "export const value = () => { import('module') }",
    )

    for (statement in statements) {
      assertTrue(
        MdxEsmScanner.findMissingStatementSeparators(statement, 0, statement.length).isEmpty(),
        statement,
      )
    }
  }

  @Test
  fun regularExpressionBracesDoNotAffectDelimiterDepth() {
    val statement = "export const pattern = /[{]/;"
    val block = MdxEsmScanner.scanBlock("$statement\n# Heading", 0)

    assertTrue(block?.terminated == true)
    assertEquals(statement.length, block?.range?.last)
  }

  @Test
  fun lexerOwnedBracesDoNotAffectDelimiterDepth() {
    val statements = listOf(
      "export const string = '}'",
      "export const template = `before } after`",
      "export const comment = /* } */ 1",
      "export const element = <div data-label=\"}\">{'{'}</div>",
    )

    for (statement in statements) {
      val block = MdxEsmScanner.scanBlock("$statement\n# Heading", 0)
      assertTrue(block?.terminated == true, statement)
      assertEquals(statement.length, block?.range?.last, statement)
    }
  }

  @Test
  fun exportedFunctionBodyMayStartOnNextLine() {
    val statement = """
      export default function Layout(props)
      {
        return props.children
      }
    """.trimIndent()
    val block = MdxEsmScanner.scanBlock("$statement\n# Heading", 0)

    assertTrue(block?.terminated == true)
    assertEquals(statement.length, block?.range?.last)
  }

  @Test
  fun nextLineMdxExpressionDoesNotBecomeAnEsmContinuation() {
    val statement = "export const value = 1"
    val block = MdxEsmScanner.scanBlock("$statement\n{value}", 0)

    assertTrue(block?.terminated == true)
    assertEquals(statement.length, block?.range?.last)
  }

  @Test
  fun multilineJsxRemainsInsideExport() {
    val statement = """
      export const element = <div data-label="}">
        {'{'}
      </div>
    """.trimIndent()
    val block = MdxEsmScanner.scanBlock("$statement\n# Heading", 0)

    assertTrue(block?.terminated == true)
    assertEquals(statement.length, block?.range?.last)
  }

  @Test
  fun unterminatedLexicalConstructsRemainUnterminated() {
    val statements = listOf(
      "export const string = 'unfinished",
      "export const string = \"unfinished\\\"",
      "export const template = `unfinished",
      "export const comment = /* unfinished",
      "export const pattern = /unfinished",
    )

    for (statement in statements) {
      val block = MdxEsmScanner.scanBlock(statement, 0)
      assertFalse(block?.terminated == true, statement)
    }
  }
}

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
  }
}

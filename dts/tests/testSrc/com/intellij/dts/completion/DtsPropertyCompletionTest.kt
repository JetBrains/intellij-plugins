package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsPropertyCompletionTest : DtsCompletionTest(needsZephyr = false) {
  @Test
  fun `test new property (compatible)`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("c", "pati"),
      lookupString = "compatible",
      input = "<caret> // comment",
    )
  }

  @Test
  fun `test edit empty property (#size-cells)`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("#", "#size", "ize-"),
      lookupString = "#size-cells",
      input = "<caret>;",
    )
  }

  @Test
  fun `test edit property with value (device_type)`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("d", "vice_", "type"),
      lookupString = "device_type",
      input = "<caret> = <40>",
    )
  }

  @Test
  fun `test edit property with label (interrupt-map-mask)`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("inter", "-", "-map-", "mask"),
      lookupString = "interrupt-map-mask",
      input = "label: <caret>;"
    )
  }

  private suspend fun doTest(
    variations: List<String>,
    lookupString: String,
    input: String,
  ) {
    for (variation in listOf("", lookupString) + variations) {
      doCompletionTest(
        lookupString,
        input = input.replace("<caret>", "$variation<caret>"),
        after = input.replace("<caret>", "$lookupString<caret>"),
        surrounding = "/ {\n<embed>\n};",
        useNodeContentVariations = true,
      )
    }
  }
}
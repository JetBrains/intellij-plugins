package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsBindingReferenceTest : DtsCompletionTest(needsZephyr = true) {
  @Test
  fun `test compatible espressif,esp32-eth`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("espressif", "eth"),
      lookupString = "espressif,esp32-eth",
    )
  }

  @Test
  fun `test compatible espressif,esp32-pinctrl`() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("espressif", "esp32-pinctrl"),
      lookupString = "espressif,esp32-pinctrl",
    )
  }

  @Test
  fun `test no completion if property name is not compatible`() = dtsTimeoutRunBlocking {
    doNoCompletionTest(
      input = "something = \"<caret>\"",
      surrounding = "/ { <embed> };",
      useNodeContentVariations = true,
    )
  }

  private suspend fun doTest(variations: List<String>, lookupString: String) {
    val input = "compatible = \"<caret>\""

    for (variation in listOf("", lookupString) + variations) {
      doCompletionTest(
        lookupString,
        input = input.replace("<caret>", "$variation<caret>"),
        after = input.replace("<caret>", "$lookupString<caret>"),
        surrounding = "/ { <embed> };",
        useNodeContentVariations = true,
      )
    }
  }
}
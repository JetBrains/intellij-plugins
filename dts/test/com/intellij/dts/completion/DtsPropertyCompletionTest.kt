package com.intellij.dts.completion

class DtsPropertyCompletionTest : DtsCompletionTest() {
  fun `test new property (compatible)`() = doTest(
    variations = listOf("c", "pati"),
    lookupString = "compatible",
    input = "<caret> // comment",
  )

  fun `test edit empty property (#size-cells)`() = doTest(
    variations = listOf("#", "#size", "ize-"),
    lookupString = "#size-cells",
    input = "<caret>;",
  )

  fun `test edit property with value (device_type)`() = doTest(
    variations = listOf("d", "vice_", "type"),
    lookupString = "device_type",
    input = "<caret> = <40>",
  )

  fun `test edit property with label (interrupt-map-mask)`() = doTest(
    variations = listOf("inter", "-", "-map-", "mask"),
    lookupString = "interrupt-map-mask",
    input = "label: <caret>;"
  )

  private fun doTest(
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
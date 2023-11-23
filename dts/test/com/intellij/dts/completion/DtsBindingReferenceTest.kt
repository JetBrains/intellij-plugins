package com.intellij.dts.completion

class DtsBindingReferenceTest : DtsCompletionTest() {
  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test compatible espressif,esp32-eth`() = doTest(
    variations = listOf("espressif", "eth"),
    lookupString = "espressif,esp32-eth",
  )

  fun `test compatible espressif,esp32-pinctrl`() = doTest(
    variations = listOf("espressif", "esp32-pinctrl"),
    lookupString = "espressif,esp32-pinctrl",
  )

  fun `test no completion if property name is not compatible`() = doNoCompletionTest(
    input = "something = \"<caret>\"",
    surrounding = "/ { <embed> };",
    useNodeContentVariations = true,
  )

  private fun doTest(variations: List<String>, lookupString: String) {
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
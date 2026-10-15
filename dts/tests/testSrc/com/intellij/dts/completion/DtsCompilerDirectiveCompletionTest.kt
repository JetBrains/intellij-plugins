package com.intellij.dts.completion

class DtsCompilerDirectiveCompletionTest : DtsCompletionTest() {
  fun testRootV1() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/d", "/dts-v1", "/v1"),
      lookupString = "/dts-v1/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  fun testRootPlugin() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/p", "/plug"),
      lookupString = "/plugin/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  fun testRootOmit() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/o", "/omit-", "/-no-ref"),
      lookupString = "/omit-if-no-ref/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  fun testNodeOmit() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/o", "/omit-", "/-no-ref"),
      lookupString = "/omit-if-no-ref/",
      input = "<caret> node {}",
      useNodeContentVariations = true,
    )
  }

  fun testInclude() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/i", "/include"),
      lookupString = "/include/",
      input = "<caret>",
      useRootContentVariations = true,
      useNodeContentVariations = true,
    )
  }

  fun testNodeInclude() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/i", "/include"),
      lookupString = "/include/",
      input = "/ { <caret> }"
    )
  }

  fun testNoLookup() {
    val contexts = listOf(
      "prop = <caret>",
      "prop = <<caret>>",
      "prop = [<caret>]",
      "prop = \"<caret>\"",
      "&<caret>",
    )

    for (context in contexts) {
      configureByText(context)

      val items = myFixture.completeBasic()
      assertNotNull(context, items)
      assertEmpty(context, items.filter { it.lookupString.startsWith('/') })
    }
  }

  private suspend fun doTest(
    variations: List<String>,
    lookupString: String,
    input: String,
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    for (variation in listOf("", "/", lookupString) + variations) {
      doCompletionTest(
        lookupString,
        input = input.replace("<caret>", "$variation<caret>"),
        after = input.replace("<caret>", "$lookupString<caret>"),
        useRootContentVariations = useRootContentVariations,
        useNodeContentVariations = useNodeContentVariations,
      )
    }
  }
}

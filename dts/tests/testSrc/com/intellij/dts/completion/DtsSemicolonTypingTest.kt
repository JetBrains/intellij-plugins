package com.intellij.dts.completion

class DtsSemicolonTypingTest : DtsCompletionTest() {
  fun testValidProperty() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <><caret>",
      after = "prop = <>;\n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testValidPropertyWithLabel() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <> label:<caret>",
      after = "prop = <> label:;\n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testValidPropertyWithComment() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <> // comment<caret>",
      after = "prop = <>; // comment\n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testInvalidPropertyAssignment() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <caret>",
      after = "prop = \n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testInSubNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "name {<caret>}",
      after = "name {\n    <caret>\n};",
      useNodeContentVariations = true,
    )
  }

  fun testAfterSubNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "name {}<caret>",
      after = "name {};\n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testCompilerDirective() = dtsTimeoutRunBlocking {
    doTest(
      input = "/dts-v1/<caret>",
      after = "/dts-v1/;\n<caret>",
      useRootContentVariations = true,
    )
  }

  fun testRootNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ {}<caret>",
      after = "/ {};\n<caret>",
      useRootContentVariations = true,
    )
  }

  fun testHandleNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "&handle {}<caret>",
      after = "&handle {};\n<caret>",
      useRootContentVariations = true,
    )
  }

  fun testNodeWithBrace() = dtsTimeoutRunBlocking {
    doTest(
      input = "node {\n    <caret>\n}",
      after = "node {\n    \n    <caret>\n};",
      useRootContentVariations = true,
    )
  }

  fun testNoCompletionNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "node {\n    <caret>",
      after = "node {\n    \n    <caret>",
      useRootContentVariations = true,
    )
  }

  fun testNoCompletionProperty() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <>\n<caret>",
      after = "prop = <>\n\n<caret>",
      useNodeContentVariations = true,
    )
  }

  fun testNoCompletionPropertyEmpty() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop <caret>",
      after = "prop \n<caret>",
      useNodeContentVariations = true,
    )
  }

  private suspend fun doTest(
    input: String,
    after: String,
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) = doTypeTest(
    character = "\n",
    input = input,
    after = after,
    useNodeContentVariations = useNodeContentVariations,
    useRootContentVariations = useRootContentVariations,
  )
}
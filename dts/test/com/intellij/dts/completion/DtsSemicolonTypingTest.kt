package com.intellij.dts.completion

class DtsSemicolonTypingTest : DtsCompletionTest() {
  fun testValidProperty() = doTest(
    input = "prop = <><caret>",
    after = "prop = <>;\n<caret>",
    useNodeContentVariations = true,
  )

  fun testValidPropertyWithLabel() = doTest(
    input = "prop = <> label:<caret>",
    after = "prop = <> label:;\n<caret>",
    useNodeContentVariations = true,
  )

  fun testValidPropertyWithComment() = doTest(
    input = "prop = <> // comment<caret>",
    after = "prop = <>; // comment\n<caret>",
    useNodeContentVariations = true,
  )

  fun testInvalidPropertyAssignment() = doTest(
    input = "prop = <caret>",
    after = "prop = \n<caret>",
    useNodeContentVariations = true,
  )

  fun testInSubNode() = doTest(
    input = "name {<caret>}",
    after = "name {\n    <caret>\n};",
    useNodeContentVariations = true,
  )

  fun testAfterSubNode() = doTest(
    input = "name {}<caret>",
    after = "name {};\n<caret>",
    useNodeContentVariations = true,
  )

  fun testCompilerDirective() = doTest(
    input = "/dts-v1/<caret>",
    after = "/dts-v1/;\n<caret>",
    useRootContentVariations = true,
  )

  fun testRootNode() = doTest(
    input = "/ {}<caret>",
    after = "/ {};\n<caret>",
    useRootContentVariations = true,
  )

  fun testHandleNode() = doTest(
    input = "&handle {}<caret>",
    after = "&handle {};\n<caret>",
    useRootContentVariations = true,
  )

  fun testNodeWithBrace() = doTest(
    input = "node {\n    <caret>\n}",
    after = "node {\n    \n    <caret>\n};",
    useRootContentVariations = true,
  )

  fun testNoCompletionNode() = doTest(
    input = "node {\n    <caret>",
    after = "node {\n    \n    <caret>",
    useRootContentVariations = true,
  )

  fun testNoCompletionProperty() = doTest(
    input = "prop = <>\n<caret>",
    after = "prop = <>\n\n<caret>",
    useNodeContentVariations = true,
  )

  fun testNoCompletionPropertyEmpty() = doTest(
    input = "prop <caret>",
    after = "prop \n<caret>",
    useNodeContentVariations = true,
  )

  private fun doTest(
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
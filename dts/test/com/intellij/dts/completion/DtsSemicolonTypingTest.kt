package com.intellij.dts.completion

class DtsSemicolonTypingTest : DtsCompletionTest() {
  fun testValidProperty() = doEnterTest(
    input = "prop = <><caret>",
    after = "prop = <>;\n<caret>",
    useNodeContentVariations = true,
  )

  fun testValidPropertyWithLabel() = doEnterTest(
    input = "prop = <> label:<caret>",
    after = "prop = <> label:;\n<caret>",
    useNodeContentVariations = true,
  )

  fun testValidPropertyWithComment() = doEnterTest(
    input = "prop = <> // comment<caret>",
    after = "prop = <>; // comment\n<caret>",
    useNodeContentVariations = true,
  )

  fun testInvalidPropertyAssignment() = doEnterTest(
    input = "prop = <caret>",
    after = "prop = \n<caret>",
    useNodeContentVariations = true,
  )

  fun testInSubNode() = doEnterTest(
    input = "name {<caret>}",
    after = "name {\n    <caret>\n};",
    useNodeContentVariations = true,
  )

  fun testAfterSubNode() = doEnterTest(
    input = "name {}<caret>",
    after = "name {};\n<caret>",
    useNodeContentVariations = true,
  )

  fun testCompilerDirective() = doEnterTest(
    input = "/dts-v1/<caret>",
    after = "/dts-v1/;\n<caret>",
    useRootContentVariations = true,
  )

  fun testRootNode() = doEnterTest(
    input = "/ {}<caret>",
    after = "/ {};\n<caret>",
    useRootContentVariations = true,
  )

  fun testHandleNode() = doEnterTest(
    input = "&handle {}<caret>",
    after = "&handle {};\n<caret>",
    useRootContentVariations = true,
  )

  fun testNodeWithBrace() = doEnterTest(
    input = "node {\n    <caret>\n}",
    after = "node {\n    \n    <caret>\n};",
    useRootContentVariations = true,
  )

  fun testNoCompletionNode() = doEnterTest(
    input = "node {\n    <caret>",
    after = "node {\n    \n    <caret>",
    useRootContentVariations = true,
  )

  fun testNoCompletionProperty() = doEnterTest(
    input = "prop = <>\n<caret>",
    after = "prop = <>\n\n<caret>",
    useNodeContentVariations = true,
  )

  fun testNoCompletionPropertyEmpty() = doEnterTest(
    input = "prop <caret>",
    after = "prop \n<caret>",
    useNodeContentVariations = true,
  )
}
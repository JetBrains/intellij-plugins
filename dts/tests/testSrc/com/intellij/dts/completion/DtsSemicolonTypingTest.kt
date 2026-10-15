package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsSemicolonTypingTest : DtsCompletionTest() {
  @Test
  fun testValidProperty() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <><caret>",
      after = "prop = <>;\n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testValidPropertyWithLabel() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <> label:<caret>",
      after = "prop = <> label:;\n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testValidPropertyWithComment() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <> // comment<caret>",
      after = "prop = <>; // comment\n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testInvalidPropertyAssignment() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <caret>",
      after = "prop = \n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testInSubNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "name {<caret>}",
      after = "name {\n    <caret>\n};",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testAfterSubNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "name {}<caret>",
      after = "name {};\n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testCompilerDirective() = dtsTimeoutRunBlocking {
    doTest(
      input = "/dts-v1/<caret>",
      after = "/dts-v1/;\n<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testRootNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ {}<caret>",
      after = "/ {};\n<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testHandleNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "&handle {}<caret>",
      after = "&handle {};\n<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testNodeWithBrace() = dtsTimeoutRunBlocking {
    doTest(
      input = "node {\n    <caret>\n}",
      after = "node {\n    \n    <caret>\n};",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testNoCompletionNode() = dtsTimeoutRunBlocking {
    doTest(
      input = "node {\n    <caret>",
      after = "node {\n    \n    <caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testNoCompletionProperty() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = <>\n<caret>",
      after = "prop = <>\n\n<caret>",
      useNodeContentVariations = true,
    )
  }

  @Test
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
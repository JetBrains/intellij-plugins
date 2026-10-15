package com.intellij.dts.completion

class DtsBraceTypingTest : DtsCompletionTest() {
  fun `test completes root node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "/ <caret>",
      after = "/ {<caret>}",
      useRootContentVariations = true,
    )
  }

  fun `test completes ref node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "&handle <caret>",
      after = "&handle {<caret>}",
      useRootContentVariations = true,
    )
  }

  fun `test completes sub node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "name <caret>",
      after = "name {<caret>}",
      useNodeContentVariations = true,
    )
  }

  fun `test no completion with closed brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "node <caret>}",
      after = "node {<caret>}",
    )
  }

  fun `test type out of brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "}",
      input = "node {<caret>}",
      after = "node {}<caret>",
    )
  }

  fun `test type not out of brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "node {<caret>}",
      after = "node {a<caret>}",
    )
  }

  fun `test completes byte array bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "[",
      input = "prop = <caret>",
      after = "prop = [<caret>]",
      useNodeContentVariations = true,
    )
  }

  fun `test no completion with closed bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "[",
      input = "prop = <caret>]",
      after = "prop = [<caret>]",
    )
  }

  fun `test type out of bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "]",
      input = "prop = [<caret>]",
      after = "prop = []<caret>",
    )
  }

  fun `test type not out of bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = [<caret>]",
      after = "prop = [a<caret>]",
    )
  }

  fun `test completes expression paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "(",
      input = "prop = <<caret>>",
      after = "prop = <(<caret>)>",
    )
  }

  fun `test no completion with closed paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "(",
      input = "prop = <<caret>)>",
      after = "prop = <(<caret>)>",
    )
  }

  fun `test type out of paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = ")",
      input = "prop = <(<caret>)>",
      after = "prop = <()<caret>>",
    )
  }

  fun `test type not out of paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = <(<caret>)>",
      after = "prop = <(a<caret>)>",
    )
  }

  fun `test completes cell array angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "prop = <caret>",
      after = "prop = <<caret>>",
      useNodeContentVariations = true,
    )
  }

  fun `test no completion with closed angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "prop = <caret>>",
      after = "prop = <<caret>>",
    )
  }

  fun `test type out of angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = ">",
      input = "prop = <<caret>>",
      after = "prop = <><caret>",
    )
  }

  fun `test type not out of angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = <<caret>>",
      after = "prop = <a<caret>>",
    )
  }

  fun `test no completion with closed pp angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "#include <caret>>",
      after = "#include <<caret>>",
    )
  }

  fun `test type not out of pp angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "#include <<caret>>",
      after = "#include <a<caret>>",
    )
  }
}
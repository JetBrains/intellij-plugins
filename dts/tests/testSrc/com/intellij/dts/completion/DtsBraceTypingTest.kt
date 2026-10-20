package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsBraceTypingTest : DtsCompletionTest(needsZephyr = false) {
  @Test
  fun `test completes root node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "/ <caret>",
      after = "/ {<caret>}",
      useRootContentVariations = true,
    )
  }

  @Test
  fun `test completes ref node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "&handle <caret>",
      after = "&handle {<caret>}",
      useRootContentVariations = true,
    )
  }

  @Test
  fun `test completes sub node brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "name <caret>",
      after = "name {<caret>}",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun `test no completion with closed brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "{",
      input = "node <caret>}",
      after = "node {<caret>}",
    )
  }

  @Test
  fun `test type out of brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "}",
      input = "node {<caret>}",
      after = "node {}<caret>",
    )
  }

  @Test
  fun `test type not out of brace`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "node {<caret>}",
      after = "node {a<caret>}",
    )
  }

  @Test
  fun `test completes byte array bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "[",
      input = "prop = <caret>",
      after = "prop = [<caret>]",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun `test no completion with closed bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "[",
      input = "prop = <caret>]",
      after = "prop = [<caret>]",
    )
  }

  @Test
  fun `test type out of bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "]",
      input = "prop = [<caret>]",
      after = "prop = []<caret>",
    )
  }

  @Test
  fun `test type not out of bracket`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = [<caret>]",
      after = "prop = [a<caret>]",
    )
  }

  @Test
  fun `test completes expression paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "(",
      input = "prop = <<caret>>",
      after = "prop = <(<caret>)>",
    )
  }

  @Test
  fun `test no completion with closed paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "(",
      input = "prop = <<caret>)>",
      after = "prop = <(<caret>)>",
    )
  }

  @Test
  fun `test type out of paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = ")",
      input = "prop = <(<caret>)>",
      after = "prop = <()<caret>>",
    )
  }

  @Test
  fun `test type not out of paren`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = <(<caret>)>",
      after = "prop = <(a<caret>)>",
    )
  }

  @Test
  fun `test completes cell array angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "prop = <caret>",
      after = "prop = <<caret>>",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun `test no completion with closed angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "prop = <caret>>",
      after = "prop = <<caret>>",
    )
  }

  @Test
  fun `test type out of angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = ">",
      input = "prop = <<caret>>",
      after = "prop = <><caret>",
    )
  }

  @Test
  fun `test type not out of angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "prop = <<caret>>",
      after = "prop = <a<caret>>",
    )
  }

  @Test
  fun `test no completion with closed pp angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "<",
      input = "#include <caret>>",
      after = "#include <<caret>>",
    )
  }

  @Test
  fun `test type not out of pp angel`() = dtsTimeoutRunBlocking {
    doTypeTest(
      character = "a",
      input = "#include <<caret>>",
      after = "#include <a<caret>>",
    )
  }
}
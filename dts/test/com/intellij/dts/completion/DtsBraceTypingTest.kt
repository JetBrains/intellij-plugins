package com.intellij.dts.completion

class DtsBraceTypingTest : DtsCompletionTest() {
  fun `test completes root node brace`() = doTypeTest(
    character = "{",
    input = "/ <caret>",
    after = "/ {<caret>}",
    useRootContentVariations = true,
  )

  fun `test completes ref node brace`() = doTypeTest(
    character = "{",
    input = "&handle <caret>",
    after = "&handle {<caret>}",
    useRootContentVariations = true,
  )

  fun `test completes sub node brace`() = doTypeTest(
    character = "{",
    input = "name <caret>",
    after = "name {<caret>}",
    useNodeContentVariations = true,
  )

  fun `test no completion with closed brace`() = doTypeTest(
    character = "{",
    input = "node <caret>}",
    after = "node {<caret>}",
  )

  fun `test type out of brace`() = doTypeTest(
    character = "}",
    input = "node {<caret>}",
    after = "node {}<caret>",
  )

  fun `test type not out of brace`() = doTypeTest(
    character = "a",
    input = "node {<caret>}",
    after = "node {a<caret>}",
  )

  fun `test completes byte array bracket`() = doTypeTest(
    character = "[",
    input = "prop = <caret>",
    after = "prop = [<caret>]",
    useNodeContentVariations = true,
  )

  fun `test no completion with closed bracket`() = doTypeTest(
    character = "[",
    input = "prop = <caret>]",
    after = "prop = [<caret>]",
  )

  fun `test type out of bracket`() = doTypeTest(
    character = "]",
    input = "prop = [<caret>]",
    after = "prop = []<caret>",
  )

  fun `test type not out of bracket`() = doTypeTest(
    character = "a",
    input = "prop = [<caret>]",
    after = "prop = [a<caret>]",
  )

  fun `test completes expression paren`() = doTypeTest(
    character = "(",
    input = "prop = <<caret>>",
    after = "prop = <(<caret>)>",
  )

  fun `test no completion with closed paren`() = doTypeTest(
    character = "(",
    input = "prop = <<caret>)>",
    after = "prop = <(<caret>)>",
  )

  fun `test type out of paren`() = doTypeTest(
    character = ")",
    input = "prop = <(<caret>)>",
    after = "prop = <()<caret>>",
  )

  fun `test type not out of paren`() = doTypeTest(
    character = "a",
    input = "prop = <(<caret>)>",
    after = "prop = <(a<caret>)>",
  )

  fun `test completes cell array angel`() = doTypeTest(
    character = "<",
    input = "prop = <caret>",
    after = "prop = <<caret>>",
    useNodeContentVariations = true,
  )

  fun `test no completion with closed angel`() = doTypeTest(
    character = "<",
    input = "prop = <caret>>",
    after = "prop = <<caret>>",
  )

  fun `test type out of angel`() = doTypeTest(
    character = ">",
    input = "prop = <<caret>>",
    after = "prop = <><caret>",
  )

  fun `test type not out of angel`() = doTypeTest(
    character = "a",
    input = "prop = <<caret>>",
    after = "prop = <a<caret>>",
  )

  fun `test no completion with closed pp angel`() = doTypeTest(
    character = "<",
    input = "#include <caret>>",
    after = "#include <<caret>>",
  )

  fun `test type not out of pp angel`() = doTypeTest(
    character = "a",
    input = "#include <<caret>>",
    after = "#include <a<caret>>",
  )
}
package com.intellij.dts.completion

class DtsStringCompletionTest : DtsQuoteCompletionTestBase(
  quote = '"',
  surrounding = """
    prop1 = "";
    prop2 = <embed>;
    prop3 = ""; 
  """,
)

class DtsCharCompletionTest : DtsQuoteCompletionTestBase(
  surrounding = "prop = <'c' <embed> 'c'>;",
  quote = '\'',
)

class DtsIncludePathCompletionTest : DtsQuoteCompletionTestBase(
  surrounding = "/include/ <embed>",
  quote = '"',
)

abstract class DtsQuoteCompletionTestBase(private val surrounding: String, private val quote: Char) : DtsCompletionTest() {
  fun `test new string`() = doQuoteTest(
    input = "<caret>",
    after = "x<caret>x",
  )

  fun `test complete empty string closing`() = doQuoteTest(
    input = "x<caret>",
    after = "xx<caret>",
  )

  fun `test complete string closing`() = doQuoteTest(
    input = "xc<caret>",
    after = "xcx<caret>",
  )

  fun `test complete empty string opening`() = doQuoteTest(
    input = "<caret>x",
    after = "x<caret>x",
  )

  fun `test complete string opening`() = doQuoteTest(
    input = "<caret>cx",
    after = "x<caret>cx",
  )

  fun `test new string after string`() = doQuoteTest(
    input = "xx<caret>",
    after = "xxx<caret>x",
  )

  fun `test remove opening`() = doDeleteTest(
    input = "x<caret>x",
    after = "<caret>",
  )

  fun `test remove closing`() = doDeleteTest(
    input = "xx<caret>",
    after = "x<caret>",
  )

  private fun doQuoteTest(
    input: String,
    after: String,
  ) = doTypeTest(
    character = quote.toString(),
    surrounding = surrounding,
    input = input.replace('x', quote),
    after = after.replace('x', quote),
  )

  private fun doDeleteTest(
    input: String,
    after: String,
  ) = doTypeTest(
    character = "\b",
    surrounding = surrounding,
    input = input.replace('x', quote),
    after = after.replace('x', quote),
  )
}


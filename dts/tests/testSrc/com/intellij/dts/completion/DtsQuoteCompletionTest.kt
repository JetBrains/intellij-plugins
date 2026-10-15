package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsStringCompletionTest : DtsQuoteCompletionTestBase(
  quote = '"',
  surrounding = """
    prop1 = "";
    prop2 = <embed>;
    prop3 = ""; 
  """,
)

@TestApplication
class DtsCharCompletionTest : DtsQuoteCompletionTestBase(
  surrounding = "prop = <'c' <embed> 'c'>;",
  quote = '\'',
)

@TestApplication
class DtsIncludePathCompletionTest : DtsQuoteCompletionTestBase(
  surrounding = "/include/ <embed>",
  quote = '"',
)

abstract class DtsQuoteCompletionTestBase(private val surrounding: String, private val quote: Char) : DtsCompletionTest() {
  @Test
  fun `test new string`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "<caret>",
      after = "x<caret>x",
    )
  }

  @Test
  fun `test complete empty string closing`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "x<caret>",
      after = "xx<caret>",
    )
  }

  @Test
  fun `test complete string closing`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "xc<caret>",
      after = "xcx<caret>",
    )
  }

  @Test
  fun `test complete empty string opening`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "<caret>x",
      after = "x<caret>x",
    )
  }

  @Test
  fun `test complete string opening`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "<caret>cx",
      after = "x<caret>cx",
    )
  }

  @Test
  fun `test new string after string`() = dtsTimeoutRunBlocking {
    doQuoteTest(
      input = "xx<caret>",
      after = "xxx<caret>x",
    )
  }

  @Test
  fun `test remove opening`() = dtsTimeoutRunBlocking {
    doDeleteTest(
      input = "x<caret>x",
      after = "<caret>",
    )
  }

  @Test
  fun `test remove closing`() = dtsTimeoutRunBlocking {
    doDeleteTest(
      input = "xx<caret>",
      after = "x<caret>",
    )
  }

  private suspend fun doQuoteTest(
    input: String,
    after: String,
  ) = doTypeTest(
    character = quote.toString(),
    surrounding = surrounding,
    input = input.replace('x', quote),
    after = after.replace('x', quote),
  )

  private suspend fun doDeleteTest(
    input: String,
    after: String,
  ) = doTypeTest(
    character = "\b",
    surrounding = surrounding,
    input = input.replace('x', quote),
    after = after.replace('x', quote),
  )
}


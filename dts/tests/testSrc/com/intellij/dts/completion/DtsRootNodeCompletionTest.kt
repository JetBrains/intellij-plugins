package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsRootNodeCompletionTest : DtsCompletionTest() {
  @Test
  fun `test empty line`() = dtsTimeoutRunBlocking {
    doTest(
      input = "<caret>",
      after = "/ {<caret>};",
    )
  }

  @Test
  fun `test single slash`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret>",
      after = "/ {<caret>};",
    )
  }

  @Test
  fun `test trailing semicolon`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret>;",
      after = "/ {<caret>};",
    )
  }

  @Test
  fun `test trailing lbrace`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret>};",
      after = "/ {<caret>};",
    )
  }

  @Test
  fun `test trailing rbrace`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret>{};",
      after = "/<caret> {};",
    )
  }

  @Test
  fun `test trailing space`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret> {};",
      after = "/<caret> {};",
    )
  }

  @Test
  fun `test no completion in node`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/<caret>",
      after = "/<caret>",
      surrounding = "/ {<embed>};",
      useRootContentVariations = false,
    )
  }

  @Test
  fun `test no completion in node like file`() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop;\n/<caret>",
      after = "prop;\n/<caret>",
      useRootContentVariations = false,
    )
  }

  @Test
  fun `test no completion after pp statement`() = dtsTimeoutRunBlocking {
    val ppStatements = listOf(
      "#define",
      "#endif",
      "#if",
      "#ifdef",
      "#ifndef",
      "elif",
      "else",
      "#include",
      "undef",
    )

    for (statement in ppStatements) {
      doTest(
        input = "#$statement<caret>",
        after = "#$statement<caret>",
        useRootContentVariations = false,
      )
    }
  }

  private suspend fun doTest(
    input: String,
    after: String,
    surrounding: String = "<embed>",
    useRootContentVariations: Boolean = true,
  ) {
    doCompletionTest(
      "/",
      input = input,
      after = after,
      surrounding = surrounding,
      useRootContentVariations = useRootContentVariations,
    )
  }
}

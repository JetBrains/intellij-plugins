package com.intellij.dts.completion

class DtsRootNodeCompletionTest : DtsCompletionTest() {
  fun `test empty line`() = doTest(
    input = "<caret>",
    after = "/ {<caret>};",
  )

  fun `test single slash`() = doTest(
    input = "/<caret>",
    after = "/ {<caret>};",
  )

  fun `test trailing semicolon`() = doTest(
    input = "/<caret>;",
    after = "/ {<caret>};",
  )

  fun `test trailing lbrace`() = doTest(
    input = "/<caret>};",
    after = "/ {<caret>};",
  )

  fun `test trailing rbrace`() = doTest(
    input = "/<caret>{};",
    after = "/<caret> {};",
  )

  fun `test trailing space`() = doTest(
    input = "/<caret> {};",
    after = "/<caret> {};",
  )

  fun `test no completion in node`() = doTest(
    input = "/<caret>",
    after = "/<caret>",
    surrounding = "/ {<embed>};",
    useRootContentVariations = false,
  )

  fun `test no completion in node like file`() = doTest(
    input = "prop;\n/<caret>",
    after = "prop;\n/<caret>",
    useRootContentVariations = false,
  )

  fun `test no completion after pp statement`() {
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

  private fun doTest(
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

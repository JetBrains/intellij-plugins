package com.intellij.dts.completion

import com.intellij.dts.zephyr.binding.DtsZephyrBundledBindings

class DtsNodeCompletionTest : DtsCompletionTest() {
  private fun bundledNodes(callback: (name: String, lookupString: String) -> Unit) {
    for (node in DtsZephyrBundledBindings.NODE_BINDINGS) {
      callback(node, node)
      callback("", node)
    }
  }

  fun `test new node (bundled nodes)`() = bundledNodes { name, lookupString ->
    doTest(
      lookupString = lookupString,
      input = "$name<caret> // comment",
      after = "$lookupString<caret> // comment",
    )
  }

  fun `test edit empty node (bundled nodes)`() = bundledNodes { name, lookupString ->
    doTest(
      lookupString = lookupString,
      input = "$name<caret> {};",
      after = "$lookupString<caret> {};",
    )
  }

  fun `test edit node with label (bundled nodes)`() = bundledNodes { name, lookupString ->
    doTest(
      lookupString = lookupString,
      input = "label: $name<caret> {};",
      after = "label: $lookupString<caret> {};",
    )
  }

  fun `test edit node with omit (bundled nodes)`() = bundledNodes { name, lookupString ->
    doTest(
      lookupString = lookupString,
      input = "/omit-if-no-ref/ $name<caret> {};",
      after = "/omit-if-no-ref/ $lookupString<caret> {};",
    )
  }

  fun `test empty line`() = doTest(
    lookupString = "chosen",
    input = "<caret>\n",
    after = "chosen {<caret>};\n",
  )

  fun `test trailing semicolon`() = doTest(
    lookupString = "chosen",
    input = "<caret>;",
    after = "chosen<caret>;",
  )

  fun `test trailing semicolon on new line`() = doTest(
    lookupString = "chosen",
    input = "<caret>\n;",
    after = "chosen {<caret>}\n;",
  )

  fun `test trailing rbrace on new line`() = doTest(
    lookupString = "chosen",
    input = "<caret>\n}",
    after = "chosen {<caret>};\n}",
  )

  fun `test trailing lbrace on new line`() = doTest(
    lookupString = "chosen",
    input = "<caret>\n{",
    after = "chosen<caret> \n{",
  )

  private fun doTest(
    lookupString: String,
    input: String,
    after: String,
    surrounding: String = "/ {<embed>};",
    useNodeContentVariations: Boolean = true,
  ) {
    doCompletionTest(
      lookupString = lookupString,
      input = input,
      after = after,
      surrounding = surrounding,
      useNodeContentVariations = useNodeContentVariations
    )
  }
}
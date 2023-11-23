package com.intellij.dts.completion

import com.intellij.dts.documentation.DtsBundledBindings

class DtsNodeCompletionTest : DtsCompletionTest() {
  private fun bundledNodes(callback: (variations: List<String>, lookupString: String) -> Unit) {
    for (node in DtsBundledBindings.entries.map { it.nodeName }) {
      val mid = node.length / 2
      val end = node.length - 1

      callback(
        listOf(node.slice(0..mid), node.slice(mid..end)),
        node
      )
    }
  }

  fun `test new node (bundled nodes)`() = bundledNodes { variations, lookupString ->
    doTest(variations, lookupString, "<caret> // comment")
  }

  fun `test edit empty node (bundled nodes)`() = bundledNodes { variations, lookupString ->
    doTest(variations, lookupString, "<caret> {};")
  }

  fun `test edit node with label (bundled nodes)`() = bundledNodes { variations, lookupString ->
    doTest(variations, lookupString, "label: <caret> {};")
  }

  fun `test edit node with omit (bundled nodes)`() = bundledNodes { variations, lookupString ->
    doTest(variations, lookupString, "/omit-if-no-ref/ <caret> {};")
  }

  private fun doTest(
    variations: List<String>,
    lookupString: String,
    input: String,
  ) {
    for (variation in listOf("", lookupString) + variations) {
      doCompletionTest(
        lookupString,
        input = input.replace("<caret>", "$variation<caret>"),
        after = input.replace("<caret>", "$lookupString<caret>"),
        surrounding = "/ {\n<embed>\n};",
        useNodeContentVariations = true,
      )
    }
  }
}
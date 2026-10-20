package com.intellij.dts.completion

import com.intellij.dts.core.zephyr.binding.DtsZephyrBundledBindings
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsNodeCompletionTest : DtsCompletionTest(needsZephyr = false) {
  private suspend fun bundledNodes(callback: suspend (name: String, lookupString: String) -> Unit) {
    for (node in DtsZephyrBundledBindings.NODE_BINDINGS) {
      callback(node, node)
      callback("", node)
    }
  }

  @Test
  fun `test new node (bundled nodes)`() = dtsTimeoutRunBlocking {
    bundledNodes { name, lookupString ->
      doTest(
        lookupString = lookupString,
        input = "$name<caret> // comment",
        after = "$lookupString<caret> // comment",
      )
    }
  }

  @Test
  fun `test edit empty node (bundled nodes)`() = dtsTimeoutRunBlocking {
    bundledNodes { name, lookupString ->
      doTest(
        lookupString = lookupString,
        input = "$name<caret> {};",
        after = "$lookupString<caret> {};",
      )
    }
  }

  @Test
  fun `test edit node with label (bundled nodes)`() = dtsTimeoutRunBlocking {
    bundledNodes { name, lookupString ->
      doTest(
        lookupString = lookupString,
        input = "label: $name<caret> {};",
        after = "label: $lookupString<caret> {};",
      )
    }
  }

  @Test
  fun `test edit node with omit (bundled nodes)`() = dtsTimeoutRunBlocking {
    bundledNodes { name, lookupString ->
      doTest(
        lookupString = lookupString,
        input = "/omit-if-no-ref/ $name<caret> {};",
        after = "/omit-if-no-ref/ $lookupString<caret> {};",
      )
    }
  }

  @Test
  fun `test empty line`() = dtsTimeoutRunBlocking {
    doTest(
      lookupString = "chosen",
      input = "<caret>\n",
      after = "chosen {<caret>};\n",
    )
  }

  @Test
  fun `test trailing semicolon`() = dtsTimeoutRunBlocking {
    doTest(
      lookupString = "chosen",
      input = "<caret>;",
      after = "chosen<caret>;",
    )
  }

  @Test
  fun `test trailing semicolon on new line`() = dtsTimeoutRunBlocking {
    doTest(
      lookupString = "chosen",
      input = "<caret>\n;",
      after = "chosen {<caret>}\n;",
    )
  }

  @Test
  fun `test trailing rbrace on new line`() = dtsTimeoutRunBlocking {
    doTest(
      lookupString = "chosen",
      input = "<caret>\n}",
      after = "chosen {<caret>};\n}",
    )
  }

  @Test
  fun `test trailing lbrace on new line`() = dtsTimeoutRunBlocking {
    doTest(
      lookupString = "chosen",
      input = "<caret>\n{",
      after = "chosen<caret> \n{",
    )
  }

  private suspend fun doTest(
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
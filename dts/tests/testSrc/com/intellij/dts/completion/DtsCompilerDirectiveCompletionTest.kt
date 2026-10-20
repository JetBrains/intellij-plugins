package com.intellij.dts.completion

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class DtsCompilerDirectiveCompletionTest : DtsCompletionTest(needsZephyr = false) {
  @Test
  fun testRootV1() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/d", "/dts-v1", "/v1"),
      lookupString = "/dts-v1/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testRootPlugin() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/p", "/plug"),
      lookupString = "/plugin/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testRootOmit() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/o", "/omit-", "/-no-ref"),
      lookupString = "/omit-if-no-ref/",
      input = "<caret>",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testNodeOmit() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/o", "/omit-", "/-no-ref"),
      lookupString = "/omit-if-no-ref/",
      input = "<caret> node {}",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testInclude() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/i", "/include"),
      lookupString = "/include/",
      input = "<caret>",
      useRootContentVariations = true,
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testNodeInclude() = dtsTimeoutRunBlocking {
    doTest(
      variations = listOf("/i", "/include"),
      lookupString = "/include/",
      input = "/ { <caret> }"
    )
  }

  @Test
  fun testNoLookup() {
    val contexts = listOf(
      "prop = <caret>",
      "prop = <<caret>>",
      "prop = [<caret>]",
      "prop = \"<caret>\"",
      "&<caret>",
    )

    for (context in contexts) {
      dts.configureByText(context)

      val items = myFixture.completeBasic()
      assertNotNull(items, context)
      assertThat(items.filter { it.lookupString.startsWith('/') }).describedAs(context).isEmpty()
    }
  }

  private suspend fun doTest(
    variations: List<String>,
    lookupString: String,
    input: String,
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    for (variation in listOf("", "/", lookupString) + variations) {
      doCompletionTest(
        lookupString,
        input = input.replace("<caret>", "$variation<caret>"),
        after = input.replace("<caret>", "$lookupString<caret>"),
        useRootContentVariations = useRootContentVariations,
        useNodeContentVariations = useNodeContentVariations,
      )
    }
  }
}

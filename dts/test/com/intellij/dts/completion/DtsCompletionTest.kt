package com.intellij.dts.completion

import com.intellij.dts.DtsTestBase
import com.intellij.dts.zephyr.binding.DtsZephyrBundledBindings
import com.intellij.testFramework.common.timeoutRunBlocking
import kotlin.time.Duration.Companion.seconds

abstract class DtsCompletionTest : DtsTestBase() {
  companion object {
    private val nodeContentVariations = listOf(
      "node {};",
      "variations_label: node {};",
      "/omit-if-no-ref/ node {};",
      "// comment",
      "/include/ \"file.dtsi\"",
      "prop = <>;",
      "/delete-property/ prop;",
      "/delete-node/ node;",
    )

    private val rootContentVariations = listOf(
      "/ {};",
      "&handel {};",
      "variations_label: &handel {};",
      "// comment",
      "/include/ \"file.dtsi\"",
      "/delete-node/ &handel;",
      "/dts-v1/;",
      "/plugin/;",
      "/memreserve/ 10 10;",
      "/omit-if-no-ref/ &handel;",
      "#include \"file.dtsi\"",
    )
  }

  override fun setUp() {
    super.setUp()

    timeoutRunBlocking(30.seconds) {
      DtsZephyrBundledBindings.getInstance().awaitInit()
    }
  }

  fun applyVariations(
    useRootContentVariations: Boolean,
    useNodeContentVariations: Boolean,
    callback: ((String) -> String) -> Unit
  ) {
    callback { it }

    if (useRootContentVariations) {
      for (variation in rootContentVariations) {
        callback { "$variation\n$it" }
        callback { "$it\n$variation" }
      }
    }

    if (useNodeContentVariations) {
      for (variation in nodeContentVariations) {
        callback { "$variation\n$it" }
        callback { "$it\n$variation" }
      }
    }
  }

  fun doTypeTest(
    character: String,
    input: String,
    after: String,
    surrounding: String = "<embed>",
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      val embeddedInput = surrounding.replace("<embed>", apply(input))
      val embeddedAfter = surrounding.replace("<embed>", apply(after))

      configureByText(embeddedInput)

      myFixture.type(character)
      myFixture.checkResult(embeddedAfter)
    }
  }

  private fun doCompletion(lookupString: String) {
    val items = myFixture.completeBasic() ?: return
    val lookupItem = items.find { it.lookupString == lookupString } ?: return
    myFixture.lookup.currentItem = lookupItem
    myFixture.type('\n')
  }

  fun doCompletionTest(
    lookupString: String,
    input: String,
    after: String,
    surrounding: String = "<embed>",
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      val embeddedInput = surrounding.replace("<embed>", apply(input))
      val embeddedAfter = surrounding.replace("<embed>", apply(after))

      configureByText(embeddedInput)
      doCompletion(lookupString)
      myFixture.checkResult(embeddedAfter)
    }
  }

  fun doNoCompletionTest(
    input: String,
    surrounding: String = "<embed>",
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      val embeddedInput = surrounding.replace("<embed>", apply(input))
      configureByText(embeddedInput)

      val items = myFixture.completeBasic()
      assertNotNull(items)
      assertEmpty(items)
    }
  }
}

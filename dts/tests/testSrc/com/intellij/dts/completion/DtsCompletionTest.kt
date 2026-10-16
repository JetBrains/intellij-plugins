package com.intellij.dts.completion

import com.intellij.dts.core.zephyr.binding.DtsZephyrBundledBindings
import com.intellij.dts.dtsFixture
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import kotlin.time.Duration.Companion.seconds

abstract class DtsCompletionTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)

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

  protected val dts by dtsFixture(projectFixture)

  protected val myFixture get() = dts.codeInsight

  @BeforeEach
  fun awaitBundledBindings() {
    timeoutRunBlocking(30.seconds) {
      DtsZephyrBundledBindings.getInstance().awaitInit()
    }
  }

  suspend fun applyVariations(
    useRootContentVariations: Boolean,
    useNodeContentVariations: Boolean,
    callback: suspend ((String) -> String) -> Unit,
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

  protected suspend fun doTypeTest(
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

      dts.configureByText(embeddedInput)

      myFixture.type(character)
      myFixture.checkResult(embeddedAfter)
    }
  }

  private suspend fun doCompletion(lookupString: String) {
    val items = myFixture.completeBasic() ?: return
    val lookupItem = items.find { it.lookupString == lookupString } ?: return
    withContext(Dispatchers.EDT) { myFixture.lookup.currentItem = lookupItem }
    myFixture.type('\n')
  }

  protected suspend fun doCompletionTest(
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

      dts.configureByText(embeddedInput)
      doCompletion(lookupString)
      myFixture.checkResult(embeddedAfter)
    }
  }

  protected suspend fun doNoCompletionTest(
    input: String,
    surrounding: String = "<embed>",
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      val embeddedInput = surrounding.replace("<embed>", apply(input))
      dts.configureByText(embeddedInput)

      val items = myFixture.completeBasic()
      assertNotNull(items)
      assertThat(items).isEmpty()
    }
  }
}

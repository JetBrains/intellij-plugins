package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class DtsLabelReferenceTest : DtsCompletionTest() {
  @BeforeEach
  fun addIncludedFiles() {
    myFixture.addFileToProject("a.dtsi", "/ { labelA: nodeA {}; };")
    myFixture.addFileToProject("b.dtsi", "/ { labelB: nodeB {}; };")
  }

  @Test
  fun testLabelAbove() = dtsTimeoutRunBlocking {
    doTest(
      input = "&<caret>",
      labels = listOf("label"),
      prefix = "/ { label: node {}; };",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testLabelBelow() = dtsTimeoutRunBlocking {
    doTest(
      input = "&<caret>",
      labels = emptyList(),
      suffix = "/ { label: node {}; };",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testLabelAboveValue() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = &<caret>",
      labels = listOf("label"),
      prefix = "/ { label: node1 {}; node2 {",
      suffix = "}; };",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testLabelBelowValue() = dtsTimeoutRunBlocking {
    doTest(
      input = "prop = &<caret>",
      labels = listOf("label"),
      prefix = "/ { ",
      suffix = "label: node {}; };",
      useNodeContentVariations = true,
    )
  }

  @Test
  fun testIncludeLabel() = dtsTimeoutRunBlocking {
    doTest(
      input = "&<caret>",
      labels = listOf("labelA"),
      prefix = "/include/ \"a.dtsi\"",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testIncludeLabels() = dtsTimeoutRunBlocking {
    doTest(
      input = "&<caret>",
      labels = listOf("labelA", "labelB"),
      prefix = "/include/ \"a.dtsi\" /include/ \"b.dtsi\"",
      useRootContentVariations = true,
    )
  }

  @Test
  fun testIncludeIndirect() = dtsTimeoutRunBlocking {
    for (i in 0 until 100) {
      myFixture.addFileToProject("$i.dtsi", "/include/ \"${i + 1}.dtsi\" / { l$i: n$i {}; };")
    }

    doTest(
      "&<caret>",
      (0 until 100).map { "l$it" },
      prefix = "/include/ \"0.dtsi\""
    )
  }

  @Test
  fun testEmptyOverlay() = dtsTimeoutRunBlocking {
    dts.addZephyr()
    myFixture.configureByText("esp32.overlay", "&<caret>")

    assertThat(myFixture.completeBasic()).hasSize(56)
  }

  private suspend fun doTest(
    input: String,
    labels: List<String>,
    prefix: String = "",
    suffix: String = "",
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      dts.configureByText("$prefix\n${apply(input)}\n$suffix")

      val lookups = myFixture.complete(CompletionType.BASIC)
        .map { it.lookupString }
        .filter { it != "variations_label" }

      if (labels.isEmpty()) {
        assertThat(lookups).isEmpty()
      }
      else {
        assertThat(lookups).containsAll(labels)
      }
    }
  }
}
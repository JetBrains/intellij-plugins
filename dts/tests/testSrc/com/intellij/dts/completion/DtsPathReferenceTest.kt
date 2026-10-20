package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class DtsPathReferenceTest : DtsCompletionTest(needsZephyr = true) {
  @BeforeEach
  fun addIncludedFiles() {
    myFixture.addFileToProject("a.dtsi", "/ { nodeA {}; };")
    myFixture.addFileToProject("b.dtsi", "/ { nodeB { subB {}; subA{}; }; };")
  }

  @Test
  fun testEmpty() = dtsTimeoutRunBlocking {
    doTest(
      input = "&{<caret>}",
      paths = emptyList(),
      useRootContentVariations = true,
    )
  }

  @Test
  fun testRoot() = dtsTimeoutRunBlocking {
    doTest(
      input = "&{/<caret>}",
      paths = listOf("/nodeA", "/nodeB"),
      useRootContentVariations = true,
    )
  }

  @Test
  fun testNested() = dtsTimeoutRunBlocking {
    doTest(
      input = "&{/nodeB/<caret>}",
      paths = listOf("/nodeB/subB", "/nodeB/subA"),
      useRootContentVariations = true,
    )
  }

  @Test
  fun testEmptyOverlay() = dtsTimeoutRunBlocking {
    myFixture.configureByText("esp32.overlay", "&{/<caret>} {};")

    assertThat(myFixture.completeBasic()).hasSize(10)
  }

  private suspend fun doTest(
    input: String,
    paths: List<String>,
    useRootContentVariations: Boolean = false,
    useNodeContentVariations: Boolean = false,
  ) {
    applyVariations(useRootContentVariations, useNodeContentVariations) { apply ->
      val text = """
                /include/ "a.dtsi"
                /include/ "b.dtsi"
                
                ${apply(input)}
            """.trimIndent()

      dts.configureByText(text)

      val lookups = myFixture.complete(CompletionType.BASIC).map { it.lookupString }

      if (paths.isEmpty()) {
        assertThat(lookups).isEmpty()
      }
      else {
        assertThat(lookups).containsAll(paths)
      }
    }
  }
}
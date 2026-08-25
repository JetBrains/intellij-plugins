package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxCodeFenceProviderTest {
  @Test
  fun unclosedFenceYieldsToActiveJsxCloser() {
    val cases = listOf(
      "<div>\n    ``````\n</div>",
      "<div>\n    ~~~~~~\n</div>",
      "<>\n    ``````\n</>",
    )

    for (text in cases) {
      val nodes = parseMdxNodes(text)
      val closerStart = text.lastIndexOf("</")
      val fence = nodes.single { it.type == MarkdownElementTypes.CODE_FENCE }
      val closingElement = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

      assertTrue(fence.endOffset <= closerStart, "$text: unclosed fence consumed the active JSX closer")
      assertEquals(closerStart, closingElement.startOffset, text)
      assertEquals(text.length, closingElement.endOffset, text)
      assertTrue(
        nodes.any {
          it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT && it.startOffset == 0 && it.endOffset == text.length
        },
        "$text: the enclosing JSX flow element did not recover",
      )
    }
  }

  @Test
  fun unclosedFenceKeepsMismatchedCloserOpaque() {
    val text = "<div>\n``````\n</span>\n</div>"
    val nodes = parseMdxNodes(text)
    val mismatchedCloser = text.indexOf("</span>")
    val activeCloser = text.indexOf("</div>")
    val fence = nodes.single { it.type == MarkdownElementTypes.CODE_FENCE }
    val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

    assertTrue(fence.endOffset >= mismatchedCloser + "</span>".length)
    assertTrue(fence.endOffset <= activeCloser)
    assertEquals(listOf("</div>"), closingElements.map { text.substring(it.startOffset, it.endOffset) })
  }

  @Test
  fun closedFenceKeepsMatchingCloserOpaque() {
    val text = "<div>\n``````md\n</div>\n``````\n</div>"
    val nodes = parseMdxNodes(text)
    val fence = nodes.single { it.type == MarkdownElementTypes.CODE_FENCE }
    val matchingTextInsideFence = text.indexOf("</div>")
    val activeCloser = text.lastIndexOf("</div>")
    val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

    assertTrue(fence.startOffset < matchingTextInsideFence && fence.endOffset > matchingTextInsideFence + "</div>".length)
    assertEquals(listOf(activeCloser), closingElements.map { it.startOffset })
    assertTrue(
      nodes.any {
        it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT && it.startOffset == 0 && it.endOffset == text.length
      },
    )
  }

  @Test
  fun closedFenceKeepsMatchingCloserOpaqueInMarkdownContainers() {
    val cases = listOf(
      "- <div>\n  ``````md\n  </div>\n  ``````\n  </div>",
      "> <div>\n> ``````md\n> </div>\n> ``````\n> </div>",
    )

    for (text in cases) {
      val nodes = parseMdxNodes(text)
      val matchingTextInsideFence = text.indexOf("</div>")
      val activeCloser = text.lastIndexOf("</div>")
      val fences = nodes.filter { it.type == MarkdownElementTypes.CODE_FENCE }
      assertEquals(1, fences.size, "$text: ${fences.map { it.startOffset..it.endOffset }}")
      val fence = fences.single()
      val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

      assertTrue(fence.startOffset < matchingTextInsideFence && fence.endOffset > matchingTextInsideFence + "</div>".length, text)
      assertEquals(listOf(activeCloser), closingElements.map { it.startOffset }, text)
    }
  }

  @Test
  fun unclosedFenceRecoveryUsesListAndBlockquoteConstraints() {
    val cases = listOf(
      "- <div>\n  ``````\n  </div>",
      "> <div>\n> ``````\n> </div>",
    )

    for (text in cases) {
      val nodes = parseMdxNodes(text)
      val closerStart = text.lastIndexOf("</div>")
      val fences = nodes.filter { it.type == MarkdownElementTypes.CODE_FENCE }
      val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }
      assertEquals(1, fences.size, "$text: fences=${fences.map { it.startOffset..it.endOffset }}")
      assertEquals(
        1,
        closingElements.size,
        "$text: nodes=${nodes.map { "${it.type}@${it.startOffset}..${it.endOffset}" }}",
      )
      val fence = fences.single()
      val closingElement = closingElements.single()

      assertTrue(fence.endOffset <= closerStart, "$text: container fence consumed the JSX closer")
      assertEquals(closerStart, closingElement.startOffset, text)
    }
  }

  @Test
  fun nestedJsxRecoversAfterUnclosedFence() {
    val text = "<Outer>\n  <Inner>\n    ``````\n  </Inner>\n</Outer>"

    assertNestedFlowElements(text, "unclosed fence in nested JSX")
  }

  @Test
  fun unclosedFenceYieldsToAncestorJsxCloser() {
    val text = "<Outer>\n  <Inner>\n    ``````\n</Outer>"

    assertNestedFlowElements(text, "unclosed fence before ancestor closer")
  }

  private fun assertNestedFlowElements(text: CharSequence, description: String) {
    val flowElements = parseMdxNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }

    assertEquals(2, flowElements.size, "$description: ${flowElements.map { it.startOffset..it.endOffset }}")
    assertEquals(flowElements.size, flowElements.map { it.startOffset to it.endOffset }.toSet().size, "$description: duplicate JSX ranges")
    val outer = flowElements.singleOrNull { it.startOffset == 0 && it.endOffset == text.length }
    assertTrue(outer != null, "$description: outer JSX does not own the complete source range")

    val deepest = flowElements.minBy { it.endOffset - it.startOffset }
    val ancestorFlows = generateSequence(deepest.parent) { it.parent }
      .filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
      .toList()
    assertEquals(1, ancestorFlows.size, "$description: ${flowElements.map { it.startOffset..it.endOffset }}")

    assertMdxFlowRangesNestedOrDisjoint(flowElements, description)
  }
}

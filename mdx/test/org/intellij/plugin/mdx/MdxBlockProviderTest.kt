package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@TestApplication
class MdxBlockProviderTest {
  @Test
  fun markdownLikeTextInExpressionAttributeBelongsToJsx() {
    val text: CharSequence = "Text <Alert value={[Target](./target.mdx)} /> end"
    val nodes = parseMdxNodes(text)

    assertTrue(nodes.any { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT })
  }

  @Test
  fun transientAutoCloseWithMixedNamesKeepsFlowRangesLaminar() {
    val text = "<Outer>\n<Item>\n<Data>\n<Item>\n<Leaf</Leaf>\n</Item>\n</Data>\n</Item>\n</Outer>"

    assertFlowRangesNestedOrDisjoint(text, "mixed-name auto-close intermediate")
  }

  @Test
  fun transientAutoCloseWithRepeatedNamesKeepsFlowRangesLaminar() {
    val text = "<Outer>\n<Item>\n<Item>\n<Item</Item>\n</Item>\n</Item>\n</Outer>"

    assertFlowRangesNestedOrDisjoint(text, "repeated-name auto-close intermediate")
  }

  @Test
  @Timeout(120)
  fun nestedJsxPrefixesRemainParseableThroughDepthOneHundred() {
    for (depth in 1..100) {
      val cases = mapOf(
        "named tags" to nestedElements(depth) { "Tag$it" },
        "fragments" to nestedElements(depth) { null },
        "repeated names" to nestedElements(depth) { "Item" },
        "auto-close intermediate" to nestedElements(depth, incompleteChild = "Pending") { "Tag$it" },
      )

      for ((description, text) in cases) {
        val flowElements = parseMdxNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
        assertMdxFlowRangesNestedOrDisjoint(flowElements, "$description at depth $depth")
      }
    }
  }

  private fun assertFlowRangesNestedOrDisjoint(text: CharSequence, description: String) {
    val flowElements = parseMdxNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
    assertTrue(flowElements.isNotEmpty(), "$description: no JSX flow elements")
    assertMdxFlowRangesNestedOrDisjoint(flowElements, description)
  }

  private fun nestedElements(
    depth: Int,
    incompleteChild: String? = null,
    nameAt: (Int) -> String?,
  ): String = buildString {
    val names = List(depth, nameAt)
    for (name in names) {
      append(if (name == null) "<>" else "<$name>")
      append('\n')
    }
    if (incompleteChild != null) {
      append("<$incompleteChild</$incompleteChild>\n")
    }
    else {
      append("body\n")
    }
    for (name in names.asReversed()) {
      append(if (name == null) "</>" else "</$name>")
      append('\n')
    }
  }.trimEnd()
}

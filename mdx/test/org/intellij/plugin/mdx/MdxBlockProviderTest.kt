package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@TestApplication
class MdxBlockProviderTest {
  @Test
  fun importAndExportDoNotInterruptParagraphs() {
    val lines = listOf(
      "import these helpers before you start.",
      "export the virtual machine as an image.",
      "import Component from './component'",
      "export default Component",
    )
    for (line in lines) {
      for (indent in 0..3) {
        val text = "Some prose.\n${" ".repeat(indent)}$line"
        val nodes = parseMdxNodes(text)
        val paragraphs = nodes.filter { it.type == MarkdownElementTypes.PARAGRAPH }
        assertEquals(listOf(text), paragraphs.map { it.text(text) }, text)
        assertTrue(nodes.none { it.type == MdxMarkdownLibElementTypes.MDX_ESM_BLOCK }, text)
      }
    }
  }

  @Test
  fun importAndExportStartBlocks() {
    val lines = listOf(
      "import these helpers before you start.",
      "export the virtual machine as an image.",
      "import Component from './component'",
      "export default Component",
    )
    for (line in lines) {
      for (prefix in listOf("", "Some prose.\n\n", "# Heading\n")) {
        val text = prefix + line
        val esm = parseMdxNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_ESM_BLOCK }
        assertEquals(listOf(line), esm.map { it.text(text) }, text)
      }
    }
  }

  @Test
  fun listDedentsInsideJsxKeepSiblingItems() {
    for (indent in listOf("", "  ", "    ", "      ", "\t")) {
      for (prefix in listOf("", "> ")) {
        for (marker in listOf("-", "1.", "- [ ]")) {
          val continuation = if (marker == "1.") "   " else "  "
          val text = "${prefix}<Box>\n" +
                     "$prefix$indent$marker one\n" +
                     "$prefix$indent$continuation- nested\n" +
                     "$prefix$indent$marker two\n" +
                     "$prefix</Box>"
          val nodes = parseMdxNodes(text)
          val jsx = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
          val list = jsx.children.single { it.type == MarkdownElementTypes.UNORDERED_LIST || it.type == MarkdownElementTypes.ORDERED_LIST }
          val items = list.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
          assertEquals(2, items.size, text)
          assertTrue(items[0].text(text).contains("one"), text)
          assertTrue(items[1].text(text).contains("two"), text)
          val nested = items[0].children.single { it.type == MarkdownElementTypes.UNORDERED_LIST }
          assertEquals(1, nested.children.count { it.type == MarkdownElementTypes.LIST_ITEM }, text)
          assertTrue(nested.text(text).contains("nested"), text)
          assertTrue(items[1].children.none { it.type == MarkdownElementTypes.UNORDERED_LIST }, text)
          assertEquals(text.length, jsx.endOffset, text)
        }
      }
    }
  }

  private fun ASTNode.text(source: String): String = source.substring(startOffset, endOffset)

  @Test
  fun jsxInListDoesNotOutliveItsItemAfterDedent() {
    val text = "<div>\n    - outer\n\n      <section>\n  - inner\n    - child\n  </section>\n- sibling\n</div>"
    val nodes = parseMdxNodes(text)
    val jsx = nodes.first { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
    assertEquals(0, jsx.startOffset)
    assertEquals(text.length, jsx.endOffset)
    assertTrue(nodes.any { it.type == MarkdownElementTypes.LIST_ITEM && it.text(text).trim() == "- sibling" })
  }

  @Test
  fun blockquoteDedentsInsideJsxKeepTheirParents() {
    for (indent in listOf("", "  ", "    ", "      ", "\t")) {
      val text = "<Box>\n$indent> outer\n$indent> > inner\n$indent>\n$indent> after\n</Box>"
      val nodes = parseMdxNodes(text)
      val jsx = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
      val quotes = nodes.filter { it.type == MarkdownElementTypes.BLOCK_QUOTE }
      assertEquals(2, quotes.size, text)
      assertEquals(jsx, quotes[0].parent, text)
      assertEquals(quotes[0], quotes[1].parent, text)
      val after = nodes.single { it.type == MarkdownElementTypes.PARAGRAPH && it.text(text).contains("after") }
      assertEquals(quotes[0], after.parent, text)
      assertEquals(text.length, jsx.endOffset, text)
    }
  }

  @Test
  fun markdownLikeTextInExpressionAttributeBelongsToJsx() {
    val text: CharSequence = "Text <Alert value={[Target](./target.mdx)} /> end"
    val nodes = parseMdxNodes(text)

    assertTrue(nodes.any { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT })
  }

  @Test
  fun nestedMultilineTemplateExpressionStaysOpaque() {
    for (body in listOf("a\nb\nc", "<ProfileCard\n  name=\"Alice Dupont\"\n  isOnline={true}\n/>")) {
      val expression = "{`$body`}"
      val text = "<Exercise>\n<CodeBlock language=\"tsx\">$expression</CodeBlock>\n</Exercise>"
      val nodes = parseMdxNodes(text)

      assertTrue(nodes.any { it.type == MdxMarkdownLibElementTypes.MDX_EXPRESSION && it.text(text) == expression }, text)
      assertEquals(2, nodes.count { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }, text)
    }
  }

  @Test
  fun markdownBlocksResumeAfterMultilineJavaScript() {
    val expression = "{\n/*\n# hidden\n- hidden\n```js\nignored\n```\n*/\n1\n}"
    for (opening in listOf("<B>$expression", "<B value=$expression>")) {
      val text = "<A>\n$opening\n# after\n</B>\n</A>"
      val nodes = parseMdxNodes(text)
      val heading = nodes.single { it.type == MarkdownElementTypes.ATX_1 }

      assertEquals("# after", heading.text(text), text)
      assertTrue(nodes.none { it.type == MarkdownElementTypes.UNORDERED_LIST || it.type == MarkdownElementTypes.CODE_FENCE }, text)
    }
  }

  @Test
  fun expressionClosingLineStaysOpaque() {
    val expression = "{`a\nb\nc`}"
    val text = "<A>\n<B>$expression\n# after\n</B>\n</A>"
    val nodes = parseMdxNodes(text)

    assertTrue(nodes.any { it.type == MdxMarkdownLibElementTypes.MDX_EXPRESSION && it.text(text) == expression })
    assertEquals("# after", nodes.single { it.type == MarkdownElementTypes.ATX_1 }.text(text))
    assertTrue(nodes.none { it.type == MarkdownElementTypes.PARAGRAPH && it.text(text).startsWith("c") })
  }

  @Test
  fun nestedMultilineExpressionPrefixesRemainParseable() {
    val text = "<A>\n<B>{`a\nb\nc`}</B>\n</A>"
    for (end in 1..text.length) {
      parseMdxNodes(text.take(end))
    }
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

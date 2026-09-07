package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxInlineElementParserTest {
  @Test
  fun nestedInlineElementsOwnTheirMarkdown() {
    val text = "Text <A><B>*one*</B><C>*two*</C></A>"
    val nodes = parseMdxNodes(text)
    val elements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    assertEquals(listOf("<A><B>*one*</B><C>*two*</C></A>", "<B>*one*</B>", "<C>*two*</C>"), elements.map { it.text(text) })
    assertEquals(elements[0], elements[1].parent)
    assertEquals(elements[0], elements[2].parent)
    val emphasis = nodes.filter { it.type == MarkdownElementTypes.EMPH }
    assertEquals(listOf("*one*", "*two*"), emphasis.map { it.text(text) })
    assertEquals(listOf(elements[1], elements[2]), emphasis.map { it.jsxOwner() })
  }

  @Test
  fun parentEmphasisCanWrapAChildWithoutEnteringItsBody() {
    val text = "Text *outside <A>*before <B>**inside**</B> after*</A> tail*"
    val nodes = parseMdxNodes(text)
    val elements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    assertEquals(2, elements.size)
    val emphasis = nodes.filter { it.type == MarkdownElementTypes.EMPH }
    assertEquals(2, emphasis.size)
    assertEquals(null, emphasis[0].jsxOwner())
    assertEquals(elements[0], emphasis[1].jsxOwner())
    val strong = nodes.single { it.type == MarkdownElementTypes.STRONG }
    assertEquals("**inside**", strong.text(text))
    assertEquals(elements[1], strong.jsxOwner())
  }

  @Test
  fun outerLinkCanWrapNestedInlineElements() {
    val text = "[before <A>one <B>two</B></A> after](./target)"
    val nodes = parseMdxNodes(text)
    val link = nodes.single { it.type == MarkdownElementTypes.INLINE_LINK }
    val elements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    assertEquals(2, elements.size)
    assertTrue(generateSequence(elements[0].parent) { it.parent }.any { it === link })
    assertEquals(elements[0], elements[1].jsxOwner())
  }

  @Test
  fun fragmentsAndSelfClosingChildrenKeepTheirOwners() {
    val text = "Text <A><><B /></></A>"
    val elements = parseMdxNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    assertEquals(listOf("<A><><B /></></A>", "<><B /></>", "<B />"), elements.map { it.text(text) })
    assertEquals(elements[0], elements[1].jsxOwner())
    assertEquals(elements[1], elements[2].jsxOwner())
  }

  @Test
  fun deeplyNestedInlineElementsKeepEveryOwner() {
    val depth = 200
    val text = "Text " + "<A>".repeat(depth) + "*body*" + "</A>".repeat(depth)
    val nodes = parseMdxNodes(text)
    val elements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    assertEquals(depth, elements.size)
    for (index in 1..<elements.size) assertEquals(elements[index - 1], elements[index].jsxOwner())
    assertEquals(elements.last(), nodes.single { it.type == MarkdownElementTypes.EMPH }.jsxOwner())
  }

  private fun ASTNode.jsxOwner(): ASTNode? {
    return generateSequence(parent) { it.parent }.firstOrNull { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
  }

  private fun ASTNode.text(source: String): String = source.substring(startOffset, endOffset)
}

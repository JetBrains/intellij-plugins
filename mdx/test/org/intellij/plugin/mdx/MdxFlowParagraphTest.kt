package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxFlowParagraphTest {
  @Test
  fun wrappedParagraphInsideNestedJsx() {
    val text = "<div>\n    <p>this\n        is\n        broken</p>\n</div>"
    assertParagraphs(text, listOf("this\n        is\n        broken"), 2)
  }

  @Test
  fun paragraphBoundariesFollowTheirJsxOwner() {
    for (openingBreak in listOf("", "\n")) {
      for (closingBreak in listOf("", "\n")) {
        for (indent in listOf("", "  ", "    ", "\t")) {
          for (lineCount in 1..4) {
            val body = (1..lineCount).joinToString("\n$indent") { "line$it" }
            val element = "<p>$openingBreak$body$closingBreak</p>"
            for (text in listOf(element, "$element\n\n", "<div>\n$element\n</div>")) {
              assertParagraphs(text, listOf(body), if (text.startsWith("<div>")) 2 else 1)
            }
          }
        }
      }
    }
  }

  @Test
  fun paragraphsRemainSeparateAcrossBlankLinesAndSiblings() {
    assertParagraphs("<p>one\ntwo\n\nthree\nfour</p>\n\nafter", listOf("one\ntwo", "three\nfour", "after"))
    assertParagraphs("<div>\n<p>one\ntwo\nthree</p>\n<p>four\nfive\nsix</p>\n</div>",
                     listOf("one\ntwo\nthree", "four\nfive\nsix"), 3)
  }

  @Test
  fun paragraphStartsAfterMultilineOpeningTag() {
    assertParagraphs("<p\n  title=\"value\"\n>one\ntwo\nthree</p>", listOf("one\ntwo\nthree"))
    assertParagraphs("<p\n  title=\"value\"\n>\none\ntwo\nthree</p>", listOf("one\ntwo\nthree"))
  }

  @Test
  fun singleLineJsxDoesNotConsumeFollowingBlocks() {
    for ((opening, closing) in listOf("<p>" to "</p>", "<>" to "</>")) {
      for (suffix in listOf("", "\n", "\n# After", "\n\nafter", "\n<p>after</p>")) {
        val text = "${opening}text$closing$suffix"
        val expected = if (suffix.contains("after")) listOf("text", "after") else listOf("text")
        val nodes = assertParagraphs(text, expected, if (suffix.contains("<p>")) 2 else 1)
        if (suffix.contains("# After")) {
          assertEquals("# After", nodes.single { it.type == MarkdownElementTypes.ATX_1 }.text(text))
        }
      }
    }
  }

  @Test
  fun emptyJsxBodiesHaveNoParagraphs() {
    for (text in listOf("<p></p>", "<p> </p>", "<></>", "<Box />")) {
      assertParagraphs(text, emptyList())
      assertParagraphs("$text\n\nafter", listOf("after"))
    }
  }

  @Test
  fun paragraphRangesPreserveMultilineWhitespace() {
    assertParagraphs("<p>text </p>", listOf("text"))
    for (separator in listOf("\n", "\r\n")) {
      val body = "one  ${separator}two "
      for (closingBreak in listOf("", separator, "$separator    ")) {
        assertParagraphs("<p>$body$closingBreak</p>", listOf(body))
        assertParagraphs("<div>$separator<p>$body$closingBreak</p>$separator</div>", listOf(body), 2)
      }
    }
  }

  @Test
  fun expressionsRemainInsideTheirParagraphs() {
    for (expression in listOf("{name.toUpperCase()}", "{\nname.toUpperCase()\n}")) {
      val text = "<div>$expression</div>"
      val nodes = assertParagraphs(text, listOf(expression))
      val expressionNode = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_EXPRESSION }
      assertEquals(MarkdownElementTypes.PARAGRAPH, expressionNode.parent?.type, text)
    }
  }

  @Test
  fun unfinishedChildKeepsCompletedParagraphs() {
    val text = "<p>first\nsecond\n\n<Child>\nunfinished"
    val paragraphs = parseMdxNodes(text).filter { it.type == MarkdownElementTypes.PARAGRAPH }
    assertEquals(listOf("first\nsecond", "unfinished"), paragraphs.map { it.text(text) })
  }

  @Test
  fun recoveryKeepsFollowingMarkdown() {
    for (element in listOf("<div>\n  Hello\n</span>", "<Outer>\n<Inner>\ntext\n</Outer>")) {
      val text = "$element\n\n# After"
      val nodes = parseMdxNodes(text)
      val heading = nodes.single { it.type == MarkdownElementTypes.ATX_1 }
      assertEquals("# After", heading.text(text), text)
      assertEquals(nodes.first(), heading.parent, text)
    }
  }

  @Test
  fun quotedParagraphKeepsQuoteMarkers() {
    val text = "> <Note>quoted note</Note>\n>\n> text after"
    val nodes = assertParagraphs(text, listOf("quoted note", "text after"))
    assertEquals(listOf("> ", ">", "> "), nodes.filter { it.type == MarkdownTokenTypes.BLOCK_QUOTE }.map { it.text(text) })
  }

  @Test
  fun wrappedParagraphPrefixesRemainParseable() {
    for (text in listOf("<div>\n<p>one\ntwo\nthree</p>\n</div>", "<div>\n<p>one\ntwo\nthree</div>")) {
      for (end in 0..text.length) {
        parseMdxNodes(text.take(end))
      }
    }
  }

  @Test
  fun fragmentsAndInlineContentKeepParagraphBoundaries() {
    assertParagraphs("<>one\n**two**\nthree</>", listOf("one\n**two**\nthree"))
    assertParagraphs("<p>one\n`</p>`\nthree</p>", listOf("one\n`</p>`\nthree"))
    assertParagraphs("<p>\none `code\nvalue </p>\ncode` three</p>", listOf("one `code\nvalue </p>\ncode` three"))
    assertParagraphs("<p>one\ntwo {value}\nthree</p>", listOf("one\ntwo {value}\nthree"))
  }

  @Test
  fun blocksInterruptParagraphsInsideJsx() {
    assertParagraphs("<p>one\ntwo\n# Heading\nthree\nfour</p>", listOf("one\ntwo", "three\nfour"))
    assertParagraphs("<p>one\ntwo\n```text\n</p>\n```\nthree\nfour</p>", listOf("one\ntwo", "three\nfour"))
    assertParagraphs("<p>one\ntwo\n- item\n\nthree\nfour</p>", listOf("one\ntwo", "item", "three\nfour"))
  }

  private fun assertParagraphs(text: String, expected: List<String>, expectedElementCount: Int = 1): List<ASTNode> {
    val nodes = parseMdxNodes(text)
    val paragraphs = nodes.filter { it.type == MarkdownElementTypes.PARAGRAPH }
    assertEquals(expected, paragraphs.map { it.text(text) }, text)
    val elements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
    assertEquals(expectedElementCount, elements.size, text)
    for (paragraph in paragraphs) {
      assertTrue(nodes.none {
        it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT &&
        it.startOffset < paragraph.endOffset && paragraph.startOffset < it.endOffset
      }, text)
      val owner = elements.filter { it.startOffset <= paragraph.startOffset && paragraph.startOffset < it.endOffset }
        .minByOrNull { it.endOffset - it.startOffset } ?: continue
      assertTrue(paragraph.endOffset <= owner.endOffset, text)
      assertTrue(generateSequence(paragraph.parent) { it.parent }.any { it === owner }, text)
    }
    assertMdxFlowRangesNestedOrDisjoint(elements, text)
    return nodes
  }

  private fun ASTNode.text(source: String): String = source.substring(startOffset, endOffset)
}

package org.intellij.plugin.mdx

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownCodeSpanScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxMarkdownCodeSpanScannerTest {
  @Test
  fun inlineCodeSpanIsOpaqueInsideInlineJsx() {
    val text: CharSequence = "before <span>`</span>` body</span> after"
    val nodes = parseMdxNodes(text)
    val jsx = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT }
    val codeSpan = nodes.single { it.type == MarkdownElementTypes.CODE_SPAN }

    assertEquals("<span>`</span>` body</span>", text.subSequence(jsx.startOffset, jsx.endOffset).toString())
    assertTrue(generateSequence(codeSpan.parent) { it.parent }.any { it === jsx })
  }

  @Test
  fun codeSpanIsOpaqueInsideFlowJsx() {
    val text: CharSequence = "<div>\n`</div>`\n</div>"
    val nodes = parseMdxNodes(text)
    val jsx = nodes.single {
      it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT && it.startOffset == 0 && it.endOffset == text.length
    }
    val codeSpan = nodes.single { it.type == MarkdownElementTypes.CODE_SPAN }
    val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

    assertEquals(listOf(text.lastIndexOf("</div>")), closingElements.map { it.startOffset })
    assertTrue(generateSequence(codeSpan.parent) { it.parent }.any { it === jsx })
  }

  @Test
  fun multilineCodeSpanIsOpaqueInsideFlowJsx() {
    val text: CharSequence = "<div>\n`before </div>\nafter`\n</div>"
    val nodes = parseMdxNodes(text)
    val jsx = nodes.single {
      it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT && it.startOffset == 0 && it.endOffset == text.length
    }
    val codeSpan = nodes.single { it.type == MarkdownElementTypes.CODE_SPAN }
    val closingElements = nodes.filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

    assertEquals(listOf(text.lastIndexOf("</div>")), closingElements.map { it.startOffset })
    assertTrue(generateSequence(codeSpan.parent) { it.parent }.any { it === jsx })
  }

  @Test
  fun unclosedCodeSpanDoesNotHideCloserAcrossMarkdownBlocks() {
    val text: CharSequence = "<div>\n`open\n\n</div>\n`later"
    val closerStart = text.indexOf("</div>")
    val closerEnd = closerStart + "</div>".length
    val nodes = parseMdxNodes(text)
    val jsx = nodes.single {
      it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT && it.startOffset == 0
    }
    val closingElement = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT }

    assertEquals(closerEnd, jsx.endOffset)
    assertEquals(closerStart, closingElement.startOffset)
  }

  @Test
  fun headingCodeSpansStartAfterThePreviousParagraph() {
    for (depth in 1..6) {
      for (prefix in listOf("", "> ")) {
        val text = "${prefix}<A>\n${prefix}before `\n$prefix${"#".repeat(depth)} head `</A>`\n$prefix</A>"
        val nodes = parseMdxNodes(text)
        val jsx = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
        val code = nodes.single { it.type == MarkdownElementTypes.CODE_SPAN }

        assertEquals(text.length, jsx.endOffset, text)
        assertEquals("`</A>`", text.substring(code.startOffset, code.endOffset), text)
        assertTrue(generateSequence(code.parent) { it.parent }.any { it.type.toString() == "Markdown:ATX_$depth" }, text)
        assertTrue(generateSequence(code.parent) { it.parent }.any { it === jsx }, text)
        assertEquals(listOf(text.lastIndexOf("</A>")), nodes.filter {
          it.type == MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT
        }.map { it.startOffset }, text)
      }
    }
  }

  @Test
  fun codeSpansFollowModifierAndSetextTransitions() {
    for (middle in listOf("- item `</A>`", "> quote `</A>`", "---\nnext `</A>`")) {
      val text = "<A>\nbefore `\n$middle\n</A>"
      val nodes = parseMdxNodes(text)
      val jsx = nodes.single { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
      val code = nodes.single { it.type == MarkdownElementTypes.CODE_SPAN }
      assertEquals(text.length, jsx.endOffset, text)
      assertEquals("`</A>`", text.substring(code.startOffset, code.endOffset), text)
      assertTrue(generateSequence(code.parent) { it.parent }.any { it === jsx }, text)
    }
  }

  @Test
  fun escapedBackticksCanCloseButCannotOpenCodeSpans() {
    val escapedOpener = "\\`not code`"
    val escapedCloser = "`code \\`"

    assertEquals(emptyList<TextRange>(), scan(escapedOpener))
    assertEquals(listOf(TextRange(0, escapedCloser.length)), scan(escapedCloser))
  }

  @Test
  fun incrementalSessionEmitsEachSpanOnce() {
    val text = "before `one`\nmiddle ``two``\nafter"
    val session = MdxMarkdownCodeSpanScanner.Session(text, 0)

    val spans = lineEnds(text).flatMap(session::advanceTo)

    assertEquals(
      listOf(
        TextRange(text.indexOf('`'), text.indexOf('`', text.indexOf('`') + 1) + 1),
        TextRange(text.indexOf("``"), text.indexOf("``", text.indexOf("``") + 2) + 2),
      ),
      spans,
    )
  }

  private fun scan(text: String): List<TextRange> {
    return MdxMarkdownCodeSpanScanner.Session(text, 0).advanceTo(text.length)
  }
}

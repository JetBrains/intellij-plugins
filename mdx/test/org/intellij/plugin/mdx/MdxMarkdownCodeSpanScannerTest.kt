package org.intellij.plugin.mdx

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
  fun escapedBackticksCanCloseButCannotOpenCodeSpans() {
    val escapedOpener = "\\`not code`"
    val escapedCloser = "`code \\`"

    assertEquals(emptyList<IntRange>(), scan(escapedOpener))
    assertEquals(listOf(0..escapedCloser.length), scan(escapedCloser))
  }

  @Test
  fun incrementalSessionEmitsEachSpanOnce() {
    val text = "before `one`\nmiddle ``two``\nafter"
    val session = MdxMarkdownCodeSpanScanner.Session(text, 0)

    val spans = lineEnds(text).flatMap(session::advanceTo)

    assertEquals(
      listOf(
        text.indexOf('`')..text.indexOf('`', text.indexOf('`') + 1) + 1,
        text.indexOf("``")..text.indexOf("``", text.indexOf("``") + 2) + 2,
      ),
      spans,
    )
  }

  private fun scan(text: String): List<IntRange> {
    return MdxMarkdownCodeSpanScanner.Session(text, 0).advanceTo(text.length)
  }
}

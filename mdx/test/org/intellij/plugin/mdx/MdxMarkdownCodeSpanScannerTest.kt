package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
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
}

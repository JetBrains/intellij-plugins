package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class MdxHtmlCommentBlockProviderTest {
  @Test
  fun closedMultilineHtmlCommentOwnsItsContent() {
    val text: CharSequence = "<!--\n<Unknown>{`body`}</Unknown>\n-->"
    val nodes = parseMdxNodes(text)

    assertEquals(1, nodes.count { it.type == MarkdownElementTypes.HTML_BLOCK })
    assertEquals(0, nodes.count { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT })
  }

  @Test
  fun closedMultilineHtmlCommentIsOpaqueInsideJsx() {
    val text: CharSequence = "<div>\n  <!--\n  <Unknown>{`body`}</Unknown>\n  -->\n</div>"
    val nodes = parseMdxNodes(text)

    assertEquals(1, nodes.count { it.type == MarkdownElementTypes.HTML_BLOCK })
    assertEquals(1, nodes.count { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT })
  }
}

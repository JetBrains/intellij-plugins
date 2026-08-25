package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownFenceScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class MdxMarkdownFenceScannerTest {
  @Test
  fun findsIndentedBacktickAndTildeFences() {
    val backticks = "  ```tsx\n{value}\n  ```\nafter"
    val tildes = "    ~~~md\n</not-a-tag>\n    ~~~\nafter"
    val backtickFenceEnd = backticks.indexOf("\nafter")
    val tildeFenceEnd = tildes.indexOf("\nafter")

    assertEquals(MdxMarkdownFenceScanner.findEnd(backticks, 0), backtickFenceEnd)
    assertEquals(MdxMarkdownFenceScanner.findEnd(tildes, 0), tildeFenceEnd)
  }

  @Test
  fun widerFenceContainsNarrowerFenceMarkers() {
    val text = "````md\n```js\nconst x = 1\n```\n````\nafter"
    val fenceEnd = text.indexOf("\nafter")

    assertEquals(MdxMarkdownFenceScanner.findEnd(text, 0), fenceEnd)
  }

  @Test
  fun rejectsInlineBackticksAndInvalidInfoStrings() {
    assertEquals(-1, MdxMarkdownFenceScanner.findEnd("``code``", 0))
    assertEquals(-1, MdxMarkdownFenceScanner.findEnd("```bad`info\ncontent\n```", 0))
  }

  @Test
  fun closingBoundariesMatchMarkdownParserInsideJsx() {
    val fences = listOf(
      "```tsx\n{value}\n```",
      "  ~~~md`allowed\ncontent\n  ~~~~   ",
      "    ````md\n``` remains content\n    ````` \t",
    )

    for (fence in fences) {
      val prefix = "<Box>\n"
      val text = "$prefix$fence\n</Box>"
      val scannerEnd = MdxMarkdownFenceScanner.findEnd(text, prefix.length)

      assertEquals(listOf(scannerEnd), markdownFenceEnds(text), fence)
    }
  }

  private fun markdownFenceEnds(text: CharSequence): List<Int> {
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_TEST_ROOT"), text)
    return buildList {
      root.accept(object : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
          if (node.type == MarkdownElementTypes.CODE_FENCE) {
            add(node.endOffset)
          }
          super.visitNode(node)
        }
      })
    }
  }
}

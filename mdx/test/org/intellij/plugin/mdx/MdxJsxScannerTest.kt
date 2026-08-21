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
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxJsxScannerTest {
  @Test
  fun scansMarkdownLikeTextInExpressionAttribute() {
    val text = "<Alert value={[Target](./target.mdx)} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun markdownLikeTextInExpressionAttributeBelongsToJsx() {
    val text: CharSequence = "Text <Alert value={[Target](./target.mdx)} /> end"
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_TEST_ROOT"), text)
    var hasJsx = false
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        if (node.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT) {
          hasJsx = true
        }
        super.visitNode(node)
      }
    })

    assertTrue(hasJsx)
  }

  @Test
  fun validRegularExpressionKeepsItsBrace() {
    val text = "<Alert value={/} /} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun scansTemplateLiteralExpressionAttribute() {
    val text = """
      <Source language="tsx" code={`
        const [state, setState] = useState<SomeType>()
      `}/>
    """.trimIndent()

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun recognizesMultilineExpressionAttributeAsJsxBlockStart() {
    assertTrue(MdxJsxScanner.isLineStartJsx("<Source code={`", 0))
    assertTrue(MdxJsxScanner.isLineStartJsx("<div onClick={(e) => {", 0))
  }

  @Test
  fun scansMultilineArrowFunctionAttribute() {
    val text = """
      <div onClick={(e) => {
          console.log(e)
      }}>
          Hello
      </div>
    """.trimIndent()

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun closedMultilineHtmlCommentOwnsItsContent() {
    val text: CharSequence = "<!--\n<Unknown>{`body`}</Unknown>\n-->"
    val nodes = parseNodes(text)

    assertEquals(1, nodes.count { it.type == MarkdownElementTypes.HTML_BLOCK })
    assertEquals(0, nodes.count { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT })
  }

  @Test
  fun closedMultilineHtmlCommentIsOpaqueInsideJsx() {
    val text: CharSequence = "<div>\n  <!--\n  <Unknown>{`body`}</Unknown>\n  -->\n</div>"
    val nodes = parseNodes(text)

    assertEquals(1, nodes.count { it.type == MarkdownElementTypes.HTML_BLOCK })
    assertEquals(1, nodes.count { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT })
  }

  @Test
  fun nestedFlowElementsRetainOwnershipAcrossMarkdownBlocks() {
    val cases = mapOf(
      "empty list item" to "<div>\n  <div>\n    - \n  </div>\n</div>",
      "unordered list item" to "<div>\n  <div>\n    - item\n  </div>\n</div>",
      "ordered list item" to "<div>\n  <div>\n    1. item\n  </div>\n</div>",
      "task list item" to "<div>\n  <div>\n    - [ ] item\n  </div>\n</div>",
      "heading" to "<div>\n  <div>\n    ## Heading\n  </div>\n</div>",
      "blockquote" to "<div>\n  <div>\n    > quote\n  </div>\n</div>",
      "fence" to "<div>\n  <div>\n    ```md\n    body\n    ```\n  </div>\n</div>",
    )

    for ((description, text) in cases) {
      assertNestedFlowElements(text, expectedDepth = 2, description)
    }
  }

  @Test
  fun threeNestedFlowElementsRetainOwnership() {
    val text = "<div>\n  <div>\n    <div>\n      - item\n    </div>\n  </div>\n</div>"

    assertNestedFlowElements(text, expectedDepth = 3, "three JSX levels")
  }

  private fun assertNestedFlowElements(text: CharSequence, expectedDepth: Int, description: String) {
    val flowElements = parseNodes(text).filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }

    assertEquals(expectedDepth, flowElements.size, "$description: ${flowElements.map { it.startOffset..it.endOffset }}")
    assertEquals(flowElements.size, flowElements.map { it.startOffset to it.endOffset }.toSet().size, "$description: duplicate JSX ranges")
    val outer = flowElements.singleOrNull { it.startOffset == 0 && it.endOffset == text.length }
    assertTrue(outer != null, "$description: outer JSX does not own the complete source range")

    val deepest = flowElements.minBy { it.endOffset - it.startOffset }
    val ancestorFlows = generateSequence(deepest.parent) { it.parent }
      .filter { it.type == MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT }
      .toList()
    assertEquals(expectedDepth - 1, ancestorFlows.size, "$description: ${flowElements.map { it.startOffset..it.endOffset }}")

    for (left in flowElements.indices) {
      for (right in left + 1..<flowElements.size) {
        val first = flowElements[left]
        val second = flowElements[right]
        val nested = first.startOffset <= second.startOffset && first.endOffset >= second.endOffset ||
                     second.startOffset <= first.startOffset && second.endOffset >= first.endOffset
        val disjoint = first.endOffset <= second.startOffset || second.endOffset <= first.startOffset
        assertTrue(
          nested || disjoint,
          "$description: intersecting JSX ranges ${first.startOffset..first.endOffset} and ${second.startOffset..second.endOffset}",
        )
      }
    }
  }

  private fun parseNodes(text: CharSequence): List<ASTNode> {
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_TEST_ROOT"), text)
    val nodes = mutableListOf<ASTNode>()
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        nodes.add(node)
        super.visitNode(node)
      }
    })
    return nodes
  }

}

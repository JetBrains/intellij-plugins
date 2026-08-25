package org.intellij.plugin.mdx

import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.junit.jupiter.api.Assertions.assertTrue

internal fun parseMdxNodes(text: CharSequence): List<ASTNode> {
  val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
    .parse(MarkdownElementType("MDX_TEST_ROOT"), text)
  return buildList {
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        add(node)
        super.visitNode(node)
      }
    })
  }
}

internal fun assertMdxFlowRangesNestedOrDisjoint(flowElements: List<ASTNode>, description: String) {
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

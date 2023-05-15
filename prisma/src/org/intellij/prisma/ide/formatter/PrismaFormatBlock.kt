package org.intellij.prisma.ide.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.util.containers.addIfNotNull

class PrismaFormatBlock(
  node: ASTNode,
  wrap: Wrap?,
  alignment: Alignment?,
  private val context: PrismaFormatBlockContext,
  private val childAlignment: PrismaChildAlignment? = null
) : AbstractBlock(node, wrap, alignment) {
  private val spacingProcessor = PrismaSpacingProcessor(this, context)
  private val indentProcessor = PrismaIndentProcessor()
  private val currentIndent = indentProcessor.getIndent(node)

  override fun getSpacing(child1: Block?, child2: Block): Spacing? {
    return spacingProcessor.createSpacing(child1, child2)
  }

  override fun isLeaf(): Boolean = node.firstChildNode == null

  override fun buildChildren(): List<Block> {
    val blocks = mutableListOf<Block>()
    val childAlignmentProvider = PrismaChildAlignmentProvider.forElement(node.psi)
    node.getChildren(null).asSequence()
      .filter { it.elementType != TokenType.WHITE_SPACE && (it.textLength > 0) }
      .forEach { createBlocks(blocks, it, childAlignmentProvider) }
    return blocks
  }

  private fun createBlocks(
    blocks: MutableList<in Block>,
    child: ASTNode,
    childAlignmentProvider: PrismaChildAlignmentProvider
  ) {
    val childElement = child.psi
    val newChildAlignment = childAlignmentProvider.findByElement(childElement) ?: childAlignment
    val newAlignment: Alignment? = newChildAlignment?.getAlignmentForElement(childElement)

    blocks.add(PrismaFormatBlock(child, null, newAlignment, context, newChildAlignment))
    blocks.addIfNotNull(newChildAlignment?.createAlignmentAnchor(childElement))
  }

  override fun getChildIndent(): Indent? = indentProcessor.getChildIndent(node)

  override fun getIndent(): Indent = currentIndent
}


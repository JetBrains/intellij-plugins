package org.intellij.prisma.ide.formatter

import com.intellij.formatting.*
import com.intellij.openapi.util.TextRange

class PrismaAnchorBlock(private val offset: Int, private val alignment: Alignment?) : Block {
  override fun getTextRange(): TextRange = TextRange(offset, offset)
  override fun getSubBlocks(): List<Block> = emptyList()
  override fun getWrap(): Wrap? = null
  override fun getIndent(): Indent? = null
  override fun getAlignment(): Alignment? = alignment
  override fun getSpacing(child1: Block?, child2: Block): Spacing? = null
  override fun getChildAttributes(newChildIndex: Int): ChildAttributes = ChildAttributes(null, null)
  override fun isIncomplete(): Boolean = false
  override fun isLeaf(): Boolean = true
}
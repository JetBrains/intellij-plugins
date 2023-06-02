package com.intellij.dts.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock

class DtsBlock(
    node: ASTNode,
    private val wrappingBuilder: DtsWrappingBuilder,
    private val spacingBuilder: DtsSpacingBuilder,
    private val parent: DtsBlock?,
) : AbstractBlock(node, wrappingBuilder.getWrap(parent, node), wrappingBuilder.getAlignment(parent, node)) {
    companion object {
        private val indentingBuilder = DtsIndentingBuilder()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(this, child1, child2)

    override fun getIndent(): Indent = indentingBuilder.getIndenting(parent, this) ?: Indent.getNoneIndent()

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes = ChildAttributes(
        indentingBuilder.getChildIndenting(this, newChildIndex),
        null,
    )

    override fun getDebugName(): String = "Dts${if (isLeaf) "Leaf" else ""}Block(${node.elementType})"

    override fun isLeaf(): Boolean = node.firstChildNode == null

    private fun isValidForBlock(node: ASTNode): Boolean {
        if (node.elementType == TokenType.WHITE_SPACE) return false

        return !node.textRange.isEmpty
    }

    override fun buildChildren(): MutableList<Block> {
        val childWrappingBuilder = DtsWrappingBuilder.childBuilder(this, wrappingBuilder)

        return node.getChildren(null).asSequence().filter(::isValidForBlock).map {
            DtsBlock(it, childWrappingBuilder, spacingBuilder, this)
        }.toMutableList()
    }
}
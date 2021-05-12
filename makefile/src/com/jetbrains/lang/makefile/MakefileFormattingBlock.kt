package com.jetbrains.lang.makefile

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.formatter.common.SettingsAwareBlock
import com.jetbrains.lang.makefile.psi.MakefileTypes

internal class MakefileFormattingBlock(
        node: ASTNode,
        private val settings: CodeStyleSettings, protected val spacing: SpacingBuilder,
        alignment: Alignment? = null, wrap: Wrap? = null
) : AbstractBlock(node, wrap, alignment), SettingsAwareBlock {
    override fun getSettings(): CodeStyleSettings = settings

    override fun isLeaf(): Boolean = subBlocks.isEmpty()

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = spacing.getSpacing(this, child1, child2)

    override fun getIndent(): Indent? {
        return Indent.getNormalIndent();
    }

    override fun buildChildren(): List<Block> {
        val newAlignment = Alignment.createAlignment()

        return this.children(node)
                .filter { it.elementType != MakefileTypes.TAB }
                .map { MakefileFormattingBlock(it, settings, spacing, newAlignment) }
                .toList()
    }

    private fun children(node: ASTNode) = sequence<ASTNode> {
        var child = node.firstChildNode;
        while(child != null) {
            yield(child)
            child = child.treeNext
        }
    }
}

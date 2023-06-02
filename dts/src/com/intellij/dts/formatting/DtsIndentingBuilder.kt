package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsUtil
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class DtsIndentingBuilder {
    private fun afterToken(parent: PsiElement, newChildIndex: Int, vararg tokens: IElementType): Boolean {
        return DtsUtil.children(parent, unfiltered = true)
            .filter { it.elementType != TokenType.WHITE_SPACE }
            .take(newChildIndex)
            .any { it.elementType in tokens }
    }

    fun getChildIndenting(parent: Block?, newChildIndex: Int): Indent? {
        val parentElement = ASTBlock.getPsiElement(parent) ?: return null

        return when (parentElement) {
            is DtsNode -> {
                val afterRBrace = afterToken(parentElement, newChildIndex, DtsTypes.RBRACE)
                if (afterRBrace) Indent.getNoneIndent() else Indent.getNormalIndent()
            }

            is DtsArray -> {
                val afterRBrace = afterToken(parentElement, newChildIndex, DtsTypes.RBRAC, DtsTypes.RANGL)
                if (afterRBrace) Indent.getNoneIndent() else Indent.getContinuationIndent()
            }

            is DtsContent -> if (parentElement.dtsContainer.isDtsRootContainer) Indent.getNoneIndent() else Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
    }

    private fun isOnNewLine(element: PsiElement): Boolean {
        return PsiTreeUtil.prevLeaf(element)?.text?.contains("\n") ?: false
    }

    private fun notIndented(block: Block): Boolean {
        return block.indent?.let { it.type == Indent.Type.NONE } ?: return true
    }

    fun getIndenting(parent: Block?, child: Block?): Indent? {
        if (parent == null || child == null) return null

        val parentElement = ASTBlock.getPsiElement(parent) ?: return null
        val childElement = ASTBlock.getPsiElement(child) ?: return null

        // comments need to be treated differently because they can be outside of NODE_CONTENT
        if (DtsTokenSets.comments.contains(childElement.elementType)) return getChildIndenting(parent, 0)

        return when {
            parentElement is DtsNodeContent && !parentElement.dtsContainer.isDtsRootContainer -> Indent.getNormalIndent()
            parentElement is DtsPropertyContent && childElement is DtsValue && isOnNewLine(childElement) -> Indent.getContinuationIndent(false)
            parentElement is DtsArray && childElement is DtsValue && notIndented(parent) -> Indent.getContinuationIndent(false)
            else -> Indent.getNoneIndent()
        }
    }
}
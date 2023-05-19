package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.*
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.psi.util.elementType

class DtsIndentingBuilder {
    fun getChildIndenting(parent: Block?): Indent? {
        val parentElement = ASTBlock.getPsiElement(parent) ?: return null

        return when (parentElement) {
            is DtsNode -> Indent.getNormalIndent()
            is DtsContent -> if (parentElement.dtsContainer.isDtsRootContainer) Indent.getNoneIndent() else Indent.getNormalIndent()
            is DtsPropertyContent, is DtsArray -> Indent.getContinuationIndent(false)
            else -> Indent.getNoneIndent()
        }
    }

    fun getIndenting(parent: Block?, child: Block?): Indent? {
        val parentElement = ASTBlock.getPsiElement(parent) ?: return null
        val childElement = ASTBlock.getPsiElement(child) ?: return null

        // comments need to be treated differently because they can be outside of NODE_CONTENT
        if (DtsTokenSets.comments.contains(childElement.elementType)) return getChildIndenting(parent)

        return when {
            parentElement is DtsNodeContent && !parentElement.dtsContainer.isDtsRootContainer -> Indent.getNormalIndent()
            parentElement is DtsPropertyContent && childElement is DtsValue -> Indent.getContinuationIndent(false)
            parentElement is DtsArray && childElement is DtsValue -> Indent.getContinuationIndent(false)
            else -> Indent.getNoneIndent()
        }
    }
}
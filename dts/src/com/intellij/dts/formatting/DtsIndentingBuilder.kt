package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsArray
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsNodeContent
import com.intellij.dts.lang.psi.DtsPropertyContent
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.DtsValue
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import kotlin.math.max

class DtsIndentingBuilder {
  private fun afterToken(parent: Block, newChildIndex: Int, vararg tokens: IElementType): Boolean {
    return parent.subBlocks
      .take(newChildIndex)
      .mapNotNull(ASTBlock::getElementType)
      .any { it in tokens }
  }

  fun getChildIndenting(parent: Block?, newChildIndex: Int): Indent? {
    if (parent == null) return null

    val parentElement = ASTBlock.getPsiElement(parent) ?: return null

    return when (parentElement) {
      is DtsEntry -> {
        // delegate getChildIndent to child before the new child

        val child = parent.subBlocks.getOrNull(max(0, newChildIndex - 1)) ?: return Indent.getNoneIndent()
        val index = child.subBlocks.size

        child.getChildAttributes(index).childIndent
      }
      is DtsNode -> {
        val afterRBrace = afterToken(parent, newChildIndex, DtsTypes.RBRACE)
        if (afterRBrace) Indent.getNoneIndent() else Indent.getNormalIndent()
      }
      is DtsArray -> {
        val afterRBrace = afterToken(parent, newChildIndex, DtsTypes.RBRACKET, DtsTypes.RANGL)
        if (afterRBrace) Indent.getNoneIndent() else Indent.getContinuationIndent()
      }
      is DtsNodeContent -> Indent.getNormalIndent()
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

    // absolute indenting of preprocessor statements
    if (childElement.elementType == DtsTypes.PP_STATEMENT) return Indent.getAbsoluteNoneIndent()

    return when {
      parentElement is DtsNodeContent -> Indent.getNormalIndent()
      parentElement is DtsPropertyContent && childElement is DtsValue && isOnNewLine(childElement) -> Indent.getContinuationIndent(
        false)
      parentElement is DtsArray && childElement is DtsValue && notIndented(parent) -> Indent.getContinuationIndent(false)
      else -> Indent.getNoneIndent()
    }
  }
}
package com.intellij.dts.ide

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.dtsRecursiveVisitor
import com.intellij.dts.util.DtsUtil
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class DtsFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean
    ) {
        if (root !is DtsFile) return

        val visitor = dtsRecursiveVisitor(DtsNode::class) { node ->
            if (!node.dtsIsComplete || node.dtsIsEmpty) return@dtsRecursiveVisitor

            val lBrace = DtsUtil.children(node, forward = true).firstOrNull { it.elementType == DtsTypes.LBRACE }
                ?: return@dtsRecursiveVisitor
            val rBrace = DtsUtil.children(node, forward = false).firstOrNull { it.elementType == DtsTypes.RBRACE }
                ?: return@dtsRecursiveVisitor

            descriptors += FoldingDescriptor(node, TextRange(lBrace.textRange.startOffset, rBrace.textRange.endOffset))
        }

        root.accept(visitor)
    }

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String = "{...}"

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = false
}
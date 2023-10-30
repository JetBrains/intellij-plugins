package com.intellij.dts.util

import com.intellij.dts.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

object DtsTreeUtil {
    /**
     * Gets the immediate parent node. Does not resolve references.
     */
    fun parentNode(element: PsiElement): DtsNode? {
        if (element is DtsRootNode) return null
        return PsiTreeUtil.findFirstParent(element, true) { it is DtsNode } as? DtsNode
    }

    /**
     * Gets all immediate parent nodes. Does not resolve references.
     */
    fun parentNodes(element: PsiElement): List<DtsNode> {
        if (element is DtsRootNode) return emptyList()

        val parents = mutableListOf<DtsNode>()

        var parent = element.parent
        while (parent != null) {
            if (parent is DtsNode) parents.add(parent)
            parent = parent.parent
        }

        return parents
    }

    /**
     * Gets the immediate parent property.
     */
    fun parentProperty(element: PsiElement): DtsProperty? {
        if (element is DtsProperty) return null

        // stop search at next parent node
        return PsiTreeUtil.findFirstParent(element, true) { it is DtsProperty || it is DtsNode } as? DtsProperty
    }

    /**
     * Get the immediate parent statement.
     */
    fun parentStatement(element: PsiElement): DtsStatement? {
        return PsiTreeUtil.findFirstParent(element, true) { it is DtsStatement } as? DtsStatement
    }

    /**
     * Gets the actual parent of a node. If the node is in a ref node. The
     * reference target is considered the parent. If the reference cannot be
     * resolved, null is returned.
     */
    fun findParentNode(node: DtsNode): DtsNode? {
        if (node is DtsRefNode) {
            return node.getDtsReferenceTarget()
        }

        val parent = PsiTreeUtil.findFirstParent(node, true) { it is DtsNode } as? DtsNode ?: return null

        if (parent is DtsRefNode) {
            return parent.getDtsReferenceTarget()
        }

        return parent
    }
}
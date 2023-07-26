package com.intellij.dts.util

import com.intellij.dts.lang.psi.*
import com.intellij.psi.util.PsiTreeUtil

object DtsTreeUtil {
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
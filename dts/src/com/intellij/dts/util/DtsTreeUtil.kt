package com.intellij.dts.util

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.*
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.containers.headTail

object DtsTreeUtil {
    /**
     * Gets the immediate parent of the node. Does not resolve references.
     */
    fun parentNode(statement: DtsStatement): DtsNode? {
        if (statement is DtsRootNode) return null
        return PsiTreeUtil.findFirstParent(statement, true) { it is DtsNode } as? DtsNode
    }

    /**
     * Gets all immediate parents of this node. Does not resolve references.
     */
    fun parentNodes(statement: DtsStatement): List<DtsNode> {
        if (statement is DtsRootNode) return emptyList()

        val parents = mutableListOf<DtsNode>()

        var element = statement.parent
        while (element != null) {
            if (element is DtsNode) parents.add(element)

            element = element.parent
        }

        return parents
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

    private fun findNode(node: DtsNode, segments: List<String>): DtsNode? {
        if (segments.isEmpty()) return node

        val (head, tail) = segments.headTail()
        val next = node.dtsContent
            ?.dtsNodes
            ?.filterIsInstance<DtsSubNode>()
            ?.firstOrNull { it.dtsName == head }
            ?: return null

        return findNode(next, tail)
    }

    private fun findNode(node: DtsNode, path: DtsPath): DtsNode? {
        if (!path.absolut) return findNode(node, path.segments)

        return when (node) {
            is DtsNode.Ref -> {
                val refPath = DtsPath.absolut(node) ?: return null
                val relativePath = refPath.relativize(path) ?: return null

                findNode(node, relativePath.segments)
            }

            is DtsNode.Root -> findNode(node, path.segments)
            is DtsNode.Sub -> null
        }

    }

    private fun <T> search(file: PsiFile, path: DtsPath, maxOffset: Int?, callback: (DtsNode) -> T?): T? {
        if (file !is DtsFile) return null

        var includes = file.dtsTopLevelIncludes.sortedBy { it.offset }
        var statements = file.dtsStatements.sortedBy { it.startOffset }

        if (maxOffset != null) {
            includes = includes.filter { it.offset <= maxOffset }
            statements = statements.filter { it.startOffset <= maxOffset }
        }

        var includesIndex = includes.size - 1
        var statementsIndex = statements.size - 1

        while (includesIndex >= 0 && statementsIndex >= 0) {
            val includeOffset = includes[includesIndex].offset
            val statementOffset = statements[statementsIndex].startOffset

            val result = if (includeOffset > statementOffset) {
                val ref = includes[includesIndex--].resolve(file) as? DtsFile

                if (ref != null) {
                    search(ref, path, null, callback)
                } else null
            } else {
                val root = statements[statementsIndex--] as? DtsNode

                if (root != null) {
                    findNode(root, path)?.let(callback)
                } else null
            }

            if (result != null) return result
        }

        for (i in includesIndex downTo 0) {
            val ref = includes[i].resolve(file) as? DtsFile ?: continue
            val result = search(ref, path, null, callback)

            if (result != null) return result
        }

        for (i in statementsIndex downTo 0) {
            val root = statements[i] as? DtsNode ?: continue
            val result = findNode(root, path)?.let(callback)

            if (result != null) return result
        }

        return null
    }

    /**
     * Searches through all appearances of this node including the one passed
     * in. This contains all declarations and references using labels.
     */
    fun <T> search(file: PsiFile, node: DtsNode, callback: (DtsNode) -> T?): T? {
        val path = DtsPath.absolut(node) ?: return null
        return search(file, path, node.startOffset, callback)
    }
}
package com.intellij.dts.util

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.containers.headTail

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
                val refPath = node.getDtsPath() ?: return null
                val relativePath = refPath.relativize(path) ?: return null

                findNode(node, relativePath.segments)
            }

            is DtsNode.Root -> findNode(node, path.segments)
            is DtsNode.Sub -> null
        }
    }

    private fun <T> search(file: PsiFile, path: DtsPath, maxOffset: Int?, visitedFiles: MutableSet<PsiFile>, callback: (DtsNode) -> T?): T? {
        if (file !is DtsFile || !visitedFiles.add(file) || !path.absolut) return null

        var includes = file.dtsTopLevelIncludes.sortedBy { it.offset }
        var statements = file.dtsStatements.sortedBy { it.startOffset }

        if (maxOffset != null) {
            includes = includes.filter { it.before(maxOffset) }
            statements = statements.filter { it.startOffset < maxOffset }
        }

        var includesIndex = includes.size - 1
        var statementsIndex = statements.size - 1

        while (includesIndex >= 0 && statementsIndex >= 0) {
            val includeOffset = includes[includesIndex].offset
            val statementOffset = statements[statementsIndex].startOffset

            val result = if (includeOffset != null && includeOffset > statementOffset) {
                val ref = includes[includesIndex--].resolve(file) as? DtsFile

                if (ref != null) {
                    search(ref, path, null, visitedFiles, callback)
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
            val result = search(ref, path, null, visitedFiles, callback)

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
     *
     * Returns the first non-null value returned by the callback.
     */
    fun <T> search(file: PsiFile, node: DtsNode, callback: (DtsNode) -> T?): T? {
        val path = node.getDtsPath() ?: return null
        return search(file, path, node.startOffset + 1, callback)
    }

    /**
     * Searches through all appearances of nodes with the specified path. If the
     * [offset] is not null, only nodes and includes before (inclusive) the
     * offset are taken into consideration.
     *
     * Returns the first non-null value returned by the callback.
     */
    fun <T> search(file: PsiFile, path: DtsPath, offset: Int?, callback: (DtsNode) -> T?): T? {
        return search(file, path, offset, mutableSetOf(), callback)
    }
}
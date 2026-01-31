package com.intellij.dts.util

import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.getDtsReferenceTarget
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
   * Gets the actual parent of a node. All references will be resolved.
   */
  fun findParentNode(node: DtsNode): DtsNode? {
    if (node is DtsRefNode) {
      return node.getDtsReferenceTarget()?.let(::findParentNode)
    }

    val parent = parentNode(node) ?: return null
    if (parent is DtsRefNode) {
      return parent.getDtsReferenceTarget()
    }

    return parent
  }
}
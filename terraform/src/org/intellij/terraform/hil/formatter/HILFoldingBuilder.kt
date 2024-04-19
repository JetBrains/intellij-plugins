// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.formatter

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes

class HILFoldingBuilder : FoldingBuilderEx(), DumbAware {
  override fun isCollapsedByDefault(node: ASTNode): Boolean {
    return false
  }

  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
    val descriptors = ArrayList<FoldingDescriptor>()
    collect(root.node, document, descriptors)
    return descriptors.toTypedArray()
  }

  companion object {
    private val Accepted = TokenSet.create(HILElementTypes.IL_PARENTHESIZED_EXPRESSION, HILElementTypes.IL_PARAMETER_LIST, HILElementTypes.IL_EXPRESSION_HOLDER)
  }

  private fun collect(node: ASTNode, document: Document, descriptors: ArrayList<FoldingDescriptor>) {
    if (Accepted.contains(node.elementType) && node.textLength > 5) {
      descriptors.add(FoldingDescriptor(node, node.textRange))
    }
    for (c in node.getChildren(null)) {
      collect(c, document, descriptors)
    }
  }

  override fun getPlaceholderText(node: ASTNode): String {
    return when (node.elementType) {
      HILElementTypes.IL_PARENTHESIZED_EXPRESSION -> "(...)"
      HILElementTypes.IL_PARAMETER_LIST -> "(...)"
      HILElementTypes.IL_EXPRESSION_HOLDER -> "\${...}"
      else -> "..."
    }
  }
}

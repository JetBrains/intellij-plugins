// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLArray
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.getNextSiblingNonWhiteSpace

class HCLFoldingBuilder : CustomFoldingBuilder(), DumbAware {
  override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
    return false
  }

  override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
    collect(root, descriptors, HashSet())
  }

  private fun collect(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, usedComments: MutableSet<PsiElement>) {
    val node = element.node
    when (node.elementType) {
      HCLElementTypes.OBJECT, HCLElementTypes.BLOCK_OBJECT -> {
        if (isSpanMultipleLines(node) && element is HCLObject) {
          val props = element.propertyList.size
          val blocks = element.blockList.size
          when (props + blocks) {
            0, 1 -> descriptors.add(FoldingDescriptor(node, node.textRange, null, getCollapsedObjectPlaceholder(element)))
            else -> descriptors.add(FoldingDescriptor(node, node.textRange))
          }
        }
      }
      HCLElementTypes.ARRAY -> {
        if (isSpanMultipleLines(node) && element is HCLArray) {
          when (element.elements.size) {
            0, 1 -> descriptors.add(FoldingDescriptor(node, node.textRange, null, getCollapsedArrayPlaceholder(element)))
            else -> descriptors.add(FoldingDescriptor(node, node.textRange))
          }
        }
      }
      HCLElementTypes.BLOCK_COMMENT -> descriptors.add(FoldingDescriptor(node, node.textRange))
      HCLElementTypes.LINE_C_COMMENT, HCLElementTypes.LINE_HASH_COMMENT -> {
        if (usedComments.add(element)) {
          if (!isCustomRegionElement(element)) {
            var end: PsiElement? = null
            var current: PsiElement? = element.getNextSiblingNonWhiteSpace()
            while (current != null) {
              if (HCLTokenTypes.HCL_LINE_COMMENTS.contains(current.node.elementType)) {
                if (isCustomRegionElement(current)) {
                  // Stop current folding
                  usedComments.add(current)
                  break
                }
                end = current
                usedComments.add(current)
                current = current.getNextSiblingNonWhiteSpace()
                continue
              }
              break
            }
            if (end != null) {
              val range = TextRange(element.textRange.startOffset, end.textRange.endOffset)
              descriptors.add(FoldingDescriptor(element, range))
            }
          }
        }
      }
    }

    val childUsedCommends = HashSet<PsiElement>()
    for (c in element.children) {
      collect(c, descriptors, childUsedCommends)
    }
  }

  private fun getCollapsedObjectPlaceholder(element: HCLObject, limit: Int = 30): String {
    val props = element.propertyList.size
    val blocks = element.blockList.size
    if (props + blocks == 0) return "{}"
    else if (props + blocks != 1) return "{...}"

    val prop = element.propertyList.firstOrNull()
    if (prop != null) {
      if (prop.textLength > limit) return "{...}"
      return "{" + prop.text + "}"
    }
    val bl = element.blockList.firstOrNull()
    if (bl != null) {
      if (bl.name.length > limit) return "{...}"
      val obj = bl.`object` ?: return "{...}"
      val inner = getCollapsedObjectPlaceholder(obj, limit - (bl.name.length + 3))
      return "{${bl.name} $inner}"
    }
    return "{}"
  }

  private fun getCollapsedArrayPlaceholder(element: HCLArray, limit: Int = 30): String {
    val vals = element.elements
    if (vals.isEmpty()) return "[]"
    if (vals.size > 1) return "[...]"
    val node = vals.first().node
    if (node.textLength > limit) return "[...]"
    return "[${node.text}]"
  }

  override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
    return when (node.elementType) {
      HCLElementTypes.ARRAY -> "[...]"
      HCLElementTypes.OBJECT -> "{...}"
      HCLElementTypes.BLOCK_OBJECT -> "{...}"
      HCLElementTypes.BLOCK_COMMENT -> "/*...*/"
      HCLElementTypes.LINE_C_COMMENT -> "//..."
      HCLElementTypes.LINE_HASH_COMMENT -> "#..."
      else -> "..."
    }
  }

  private fun isSpanMultipleLines(node: ASTNode): Boolean {
    return node.textContains('\n') || node.textContains('\r')
  }
}

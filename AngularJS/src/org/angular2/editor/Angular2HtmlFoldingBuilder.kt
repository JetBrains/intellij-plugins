// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.xml.XmlFoldingBuilder
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlElement
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockParameters

class Angular2HtmlFoldingBuilder : XmlFoldingBuilder() {
  override fun doAddForChildren(tag: XmlElement, descriptors: MutableList<FoldingDescriptor>, document: Document) {
    tag.children.filterIsInstance<Angular2HtmlBlock>().forEach {
      ProgressManager.checkCanceled()
      tryAppendBlock(it, descriptors, document)
    }
    super.doAddForChildren(tag, descriptors, document)
  }

  private fun tryAppendBlock(block: Angular2HtmlBlock, descriptors: MutableList<FoldingDescriptor>, document: Document) {
    if (block.isPrimary || block.primaryBlockDefinition?.hasNestedSecondaryBlocks == true) {
      val foldingGroup = FoldingGroup.newGroup(block.getName())

      fun Angular2HtmlBlock.addFolding() {
        childrenOfType<Angular2HtmlBlockParameters>()
          .firstOrNull()
          ?.takeIf { it.textLength > 6 }
          ?.let { parameters ->
            val range = parameters.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
            descriptors.add(FoldingDescriptor(this.node, range, foldingGroup, "..."))
          }
        contents?.let { contents ->
          val range = contents.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
          if (range.length > 0) {
            descriptors.add(FoldingDescriptor(this.node, range, foldingGroup, "..."))
          }
        }
      }
      block.addFolding()
      if (block.isPrimary) {
        block.blockSiblingsForward().forEach { it.addFolding() }
      }
    }
    block.contents?.let { doAddForChildren(it, descriptors, document) }
  }

  override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
    if (node.psi is Angular2HtmlBlock) return "..."
    return super.getLanguagePlaceholderText(node, range)
  }

  private fun isSingleLine(element: PsiElement, document: Document): Boolean {
    val range = element.textRange
    return document.getLineNumber(range.startOffset) == document.getLineNumber(range.endOffset)
  }

}
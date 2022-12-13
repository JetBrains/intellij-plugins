package org.intellij.prisma.ide.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.prisma.lang.psi.PrismaBlock

class PrismaFoldingBuilder : FoldingBuilderEx(), DumbAware {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
    val blocks = PsiTreeUtil.findChildrenOfAnyType(root, PrismaBlock::class.java)
    return blocks.map { FoldingDescriptor(it, it.textRange) }.toTypedArray()
  }

  override fun getPlaceholderText(node: ASTNode): String = "{...}"

  override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
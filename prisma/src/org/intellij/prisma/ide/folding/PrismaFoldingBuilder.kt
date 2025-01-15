package org.intellij.prisma.ide.folding

import com.intellij.codeInsight.folding.CodeFoldingSettings
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.prisma.ide.documentation.buildDocComments
import org.intellij.prisma.lang.psi.DOC_COMMENT
import org.intellij.prisma.lang.psi.PrismaBlock
import org.intellij.prisma.lang.psi.PrismaElementTypes.TRIPLE_COMMENT

private const val DOT_DOT_DOT = "..."
private const val PLACEHOLDER_BLOCK = "{...}"
private const val PLACEHOLDER_DOC_COMMENT = "/**...*/"
private const val PLACEHOLDER_TRIPLE_COMMENT = "///..."

class PrismaFoldingBuilder : FoldingBuilderEx(), DumbAware {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
    return PsiTreeUtil.findChildrenOfAnyType(root, PrismaBlock::class.java).asSequence()
      .plus(buildDocComments(root))
      .map { FoldingDescriptor(it, it.textRange) }
      .toList()
      .toTypedArray()
  }

  override fun getPlaceholderText(node: ASTNode): String {
    val elementType = node.elementType
    val element = node.psi

    return when {
      elementType == TRIPLE_COMMENT -> PLACEHOLDER_TRIPLE_COMMENT
      elementType == DOC_COMMENT -> PLACEHOLDER_DOC_COMMENT
      element is PrismaBlock -> PLACEHOLDER_BLOCK
      else -> DOT_DOT_DOT
    }
  }

  override fun isCollapsedByDefault(node: ASTNode): Boolean {
    val settings = CodeFoldingSettings.getInstance();

    return when (node.elementType) {
      DOC_COMMENT -> settings.COLLAPSE_DOC_COMMENTS
      TRIPLE_COMMENT -> settings.COLLAPSE_DOC_COMMENTS
      else -> false
    }
  }
}
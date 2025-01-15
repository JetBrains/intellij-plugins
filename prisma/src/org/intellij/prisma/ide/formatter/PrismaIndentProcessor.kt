package org.intellij.prisma.ide.formatter

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType.WHITE_SPACE
import org.intellij.prisma.lang.psi.*


class PrismaIndentProcessor {
  fun getIndent(node: ASTNode): Indent {
    val elementType = node.elementType
    val parent: ASTNode? = node.treeParent

    if (parent == null || parent.treeParent == null) {
      return Indent.getNoneIndent()
    }

    if (elementType == DOC_COMMENT_LEADING_ASTERISK || elementType == DOC_COMMENT_END) {
      return Indent.getSpaceIndent(1, true)
    }

    val parentType = parent.elementType
    if (parentType in PRISMA_BLOCKS) {
      if (elementType in PRISMA_BRACES) {
        return Indent.getNoneIndent()
      }
      if (elementType in PRISMA_COMMENTS) {
        val prev: ASTNode? = node.treePrev
        // trailing comments
        if (prev?.elementType != WHITE_SPACE || prev?.textContains('\n') != true) {
          return Indent.getNoneIndent()
        }
      }

      return Indent.getNormalIndent()
    }

    return Indent.getNoneIndent()
  }

  fun getChildIndent(node: ASTNode): Indent? {
    if (node.elementType == DOC_COMMENT) {
      return Indent.getSpaceIndent(1)
    }

    return when (node.psi) {
      is PsiFile -> Indent.getNoneIndent()
      is PrismaFieldDeclarationBlock, is PrismaKeyValueBlock, is PrismaEnumDeclarationBlock -> Indent.getNormalIndent()
      else -> Indent.getNoneIndent()
    }
  }
}
package org.intellij.prisma.ide.formatter

import com.intellij.formatting.Alignment
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import org.intellij.prisma.lang.psi.PRISMA_TYPES
import org.intellij.prisma.lang.psi.PrismaElementTypes.EQ
import org.intellij.prisma.lang.psi.PrismaElementTypes.FIELD_ATTRIBUTE
import org.intellij.prisma.lang.psi.PrismaFieldAttribute
import org.intellij.prisma.lang.psi.PrismaFieldType

class PrismaChildAlignment(
  private val typeAlignment: Alignment?,
  private val attributeAlignment: Alignment?,
  private val keyValueAlignment: Alignment?
) {
  fun getAlignmentForElement(element: PsiElement): Alignment? {
    return when (element.elementType) {
      EQ -> keyValueAlignment

      in PRISMA_TYPES -> typeAlignment

      FIELD_ATTRIBUTE -> {
        // align only by the first attribute in the list
        val prev = PsiTreeUtil.skipWhitespacesBackward(element)
        return if (prev is PrismaFieldType) {
          attributeAlignment
        }
        else {
          null
        }
      }

      else -> null
    }
  }

  fun createAlignmentAnchor(node: ASTNode): PrismaAnchorBlock? {
    if (node.elementType in PRISMA_TYPES && attributeAlignment != null) {
      val element = node.psi
      val next = PsiTreeUtil.skipWhitespacesForward(element)
      if (next !is PrismaFieldAttribute) {
        return PrismaAnchorBlock(element.endOffset, attributeAlignment)
      }
    }

    return null
  }
}

package org.intellij.prisma.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.elementType
import org.intellij.prisma.ide.documentation.collectPrecedingInlineDocComments
import org.intellij.prisma.ide.documentation.prevDocComment
import org.intellij.prisma.ide.documentation.trailingDocComment

interface PrismaDocumentationOwner : PrismaNamedElement {
  val docComment: PrismaDocComment?
    get() {
      val preceding = prevDocComment()
      if (preceding is PrismaDocComment && preceding.elementType == DOC_COMMENT) {
        return preceding
      }

      val comments = collectPrecedingInlineDocComments()
      if (comments.isNotEmpty()) {
        return PrismaCompoundInlineDocumentationComment(comments)
      }

      val trailing = trailingDocComment
      if (trailing is PrismaDocComment && trailing.elementType == DOC_COMMENT) {
        return trailing
      }
      if (trailing is PsiComment && trailing.elementType == PrismaElementTypes.TRIPLE_COMMENT) {
        return PrismaCompoundInlineDocumentationComment(listOf(trailing))
      }

      return null
    }
}
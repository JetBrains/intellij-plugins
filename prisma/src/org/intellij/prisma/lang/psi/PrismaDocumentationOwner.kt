package org.intellij.prisma.lang.psi

import org.intellij.prisma.ide.documentation.collectPrecedingDocComments
import org.intellij.prisma.ide.documentation.trailingDocComment

interface PrismaDocumentationOwner : PrismaNamedElement {
  val docComment: PrismaVirtualDocumentationComment?
    get() {
      val comments = collectPrecedingDocComments()
      if (comments.isNotEmpty()) {
        return PrismaVirtualDocumentationComment(comments)
      }

      val trailing = trailingDocComment
      if (trailing != null) {
        return PrismaVirtualDocumentationComment(listOf(trailing))
      }

      return null
    }
}
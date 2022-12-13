package org.intellij.prisma.lang.psi

import com.intellij.psi.util.PsiTreeUtil

interface PrismaMemberDeclaration : PrismaNamedElement, PrismaDocumentationOwner {
  val containingDeclaration get() = PsiTreeUtil.getParentOfType(this, PrismaDeclaration::class.java)
}
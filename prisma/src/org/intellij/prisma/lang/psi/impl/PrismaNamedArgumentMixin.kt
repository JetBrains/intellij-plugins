package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.intellij.prisma.lang.psi.PrismaNamedArgument
import org.intellij.prisma.lang.resolve.PrismaReference

abstract class PrismaNamedArgumentMixin(node: ASTNode) : PrismaReferenceElementBase(node), PrismaNamedArgument {

  override val referenceNameElement: PsiElement
    get() = identifier


  override fun createReference(): PrismaReference? {
    return null
  }
}
package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.intellij.prisma.lang.psi.PrismaReferenceElement
import org.intellij.prisma.lang.psi.PrismaTypeReference
import org.intellij.prisma.lang.resolve.PrismaReference
import org.intellij.prisma.lang.resolve.PrismaTypeNameReference

abstract class PrismaTypeReferenceMixin(node: ASTNode) :
  PrismaReferenceElementBase(node),
  PrismaTypeReference,
  PrismaReferenceElement {

  override val referenceNameElement: PsiElement?
    get() = identifier ?: unsupportedType?.identifier

  override fun createReference(): PrismaReference? {
    return PrismaTypeNameReference.create(this)
  }
}
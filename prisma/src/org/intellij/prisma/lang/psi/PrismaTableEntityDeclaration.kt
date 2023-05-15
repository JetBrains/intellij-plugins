package org.intellij.prisma.lang.psi

interface PrismaTableEntityDeclaration : PrismaEntityDeclaration {
  fun getFieldDeclarationBlock(): PrismaFieldDeclarationBlock?
}
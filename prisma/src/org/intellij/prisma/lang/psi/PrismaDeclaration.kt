package org.intellij.prisma.lang.psi

interface PrismaDeclaration : PrismaNameIdentifierOwner, PrismaDocumentationOwner {
  fun getBlock(): PrismaBlock?

  fun getMembers(): List<PrismaMemberDeclaration>

  fun findMemberByName(name: String): PrismaMemberDeclaration?
}
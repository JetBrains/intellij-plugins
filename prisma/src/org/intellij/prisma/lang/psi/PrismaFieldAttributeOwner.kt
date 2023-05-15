package org.intellij.prisma.lang.psi

interface PrismaFieldAttributeOwner : PrismaMemberDeclaration {
  val fieldAttributeList: List<PrismaFieldAttribute>
}
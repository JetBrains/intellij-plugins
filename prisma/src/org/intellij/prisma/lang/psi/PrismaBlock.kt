package org.intellij.prisma.lang.psi

interface PrismaBlock : PrismaElement {
  fun getMembers(): List<PrismaElement>

  fun findMemberByName(name: String): PrismaNamedElement?

  fun getMembersByName(): Map<String, PrismaNamedElement>
}
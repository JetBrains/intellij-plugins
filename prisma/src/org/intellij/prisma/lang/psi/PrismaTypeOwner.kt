package org.intellij.prisma.lang.psi

import org.intellij.prisma.lang.types.PrismaType

interface PrismaTypeOwner : PrismaElement {
  val type: PrismaType
}
package org.intellij.prisma.lang.psi

import org.intellij.prisma.lang.types.PrismaType

interface PrismaTypeSignature : PrismaElement {
  val type: PrismaType
}
package org.intellij.prisma.lang.types

import com.intellij.psi.PsiNamedElement

interface PrismaType

interface PrismaResolvableType {
  fun resolveDeclaration(): PsiNamedElement?
}

interface PrismaDecoratedType : PrismaType {
  val innerType: PrismaType

  fun unwrap(): PrismaType {
    var type = innerType
    while (type is PrismaDecoratedType) {
      type = type.innerType
    }
    return type
  }
}

class PrismaOptionalType(override val innerType: PrismaType) : PrismaDecoratedType
class PrismaListType(override val innerType: PrismaType) : PrismaDecoratedType
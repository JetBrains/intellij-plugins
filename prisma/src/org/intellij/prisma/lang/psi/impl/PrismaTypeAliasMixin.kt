package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaTypeAlias
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaTypeAliasMixin : PrismaDeclarationMixin<PrismaNamedStub<PrismaTypeAlias>>, PrismaDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaNamedStub<PrismaTypeAlias>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
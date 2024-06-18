package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaTypeAliasStub

abstract class PrismaTypeAliasMixin : PrismaDeclarationMixin<PrismaTypeAliasStub>, PrismaDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaTypeAliasStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
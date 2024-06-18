package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaEnumDeclarationStub

abstract class PrismaEnumDeclarationMixin : PrismaDeclarationMixin<PrismaEnumDeclarationStub>, PrismaDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaEnumDeclarationStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
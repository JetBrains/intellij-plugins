package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaEnumDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaEnumDeclarationMixin : PrismaDeclarationMixin<PrismaNamedStub<PrismaEnumDeclaration>>, PrismaDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaNamedStub<PrismaEnumDeclaration>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
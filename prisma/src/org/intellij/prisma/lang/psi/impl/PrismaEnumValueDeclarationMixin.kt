package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaEnumValueDeclaration
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaEnumValueDeclarationMixin : PrismaNamedElementImpl<PrismaNamedStub<PrismaEnumValueDeclaration>>, PrismaMemberDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaNamedStub<PrismaEnumValueDeclaration>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
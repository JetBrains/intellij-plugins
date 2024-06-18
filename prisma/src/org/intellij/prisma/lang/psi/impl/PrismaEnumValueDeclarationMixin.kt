package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaEnumValueDeclarationStub

abstract class PrismaEnumValueDeclarationMixin : PrismaNamedElementImpl<PrismaEnumValueDeclarationStub>, PrismaMemberDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaEnumValueDeclarationStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
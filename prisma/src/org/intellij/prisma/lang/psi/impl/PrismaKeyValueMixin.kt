package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaKeyValueStub

abstract class PrismaKeyValueMixin : PrismaNamedElementImpl<PrismaKeyValueStub>, PrismaMemberDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaKeyValueStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
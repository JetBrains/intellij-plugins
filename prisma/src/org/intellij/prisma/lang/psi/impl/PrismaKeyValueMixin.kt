package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaKeyValueMixin : PrismaNamedElementImpl<PrismaNamedStub<PrismaKeyValue>>, PrismaMemberDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: PrismaNamedStub<PrismaKeyValue>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
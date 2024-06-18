package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaKeyValueDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaKeyValueDeclarationMixin<S : PrismaNamedStub<*>> : PrismaDeclarationMixin<S>, PrismaKeyValueDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
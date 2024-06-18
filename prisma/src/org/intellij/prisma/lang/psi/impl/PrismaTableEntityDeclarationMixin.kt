package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.intellij.prisma.lang.psi.PrismaTableEntityDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaNamedStub

abstract class PrismaTableEntityDeclarationMixin<S : PrismaNamedStub<*>> : PrismaDeclarationMixin<S>, PrismaTableEntityDeclaration {
  constructor(node: ASTNode) : super(node)

  constructor(stub: S, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
}
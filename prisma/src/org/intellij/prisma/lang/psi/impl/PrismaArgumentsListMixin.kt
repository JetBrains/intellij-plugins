package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaArgumentsList
import org.intellij.prisma.lang.psi.PrismaNamedArgument

abstract class PrismaArgumentsListMixin(node: ASTNode) : PrismaElementImpl(node), PrismaArgumentsList {
  override fun findArgumentByName(name: String): PrismaNamedArgument? {
    return arguments.filterIsInstance<PrismaNamedArgument>().find { it.referenceName == name }
  }
}
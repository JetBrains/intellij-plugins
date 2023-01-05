package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaTableEntityDeclaration

abstract class PrismaTableEntityDeclarationMixin(node: ASTNode) :
  PrismaDeclarationMixin(node),
  PrismaTableEntityDeclaration
package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaModelTypeDeclaration

abstract class PrismaModelTypeDeclarationMixin(node: ASTNode) :
  PrismaDeclarationMixin(node),
  PrismaModelTypeDeclaration
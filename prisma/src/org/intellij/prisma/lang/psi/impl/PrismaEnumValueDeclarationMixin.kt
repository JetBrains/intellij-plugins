package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration

abstract class PrismaEnumValueDeclarationMixin(node: ASTNode) : PrismaNamedElementImpl(node), PrismaMemberDeclaration
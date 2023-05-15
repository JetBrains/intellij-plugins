package org.intellij.prisma.lang.resolve

import org.intellij.prisma.lang.psi.PrismaFieldDeclaration
import org.intellij.prisma.lang.psi.PrismaFunctionCall

object PrismaResolver {
  fun isFieldExpression(function: PrismaFunctionCall) =
    function.pathExpression.resolve() is PrismaFieldDeclaration
}
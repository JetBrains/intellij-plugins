package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaNumericLiteralExpression

abstract class PrismaNumericLiteralExpressionMixin(node: ASTNode) : PrismaLiteralExpressionImpl(node), PrismaNumericLiteralExpression {
  override fun getValue(): Any? = numericLiteral.text?.toDoubleOrNull()
}
package org.intellij.prisma.lang.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import org.intellij.prisma.lang.psi.PrismaLiteralExpression

abstract class PrismaLiteralExpressionMixin(node: ASTNode) : PrismaExpressionImpl(node), PrismaLiteralExpression {
  override fun getValue(): Any? {
    val number = numericLiteral?.text?.toDoubleOrNull()
    if (number != null) {
      return number
    }
    val string = stringLiteral?.let { StringUtil.unquoteString(it.text) }
    if (string != null) {
      return string
    }
    return null
  }
}
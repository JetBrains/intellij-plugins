// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.impl

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import org.intellij.terraform.hil.HILTokenTypes
import org.intellij.terraform.hil.psi.*

object HILPsiImplUtils {
  
  fun getQualifier(expression: ILMethodCallExpression): ILExpression? {
    val select = expression.callee
    return if (select === expression.method) null else select
  }

  fun getMethod(expression: ILMethodCallExpression): ILVariable? {
    val sibling = expression.parameterList.prevSibling
    if (sibling is ILVariable) {
      return sibling
    }
    return null
  }


  fun getName(variable: ILVariable): String {
    return variable.text
  }

  fun getUnquotedText(literal: ILLiteralExpression): String {
    val dqs = literal.doubleQuotedString
    if (dqs != null) {
      return StringUtil.unquoteString(dqs.text)
    }
    return literal.text
  }

  fun getOperationSign(expression: ILUnaryExpression): IElementType {
    val nodes = expression.node.getChildren(HILTokenTypes.IL_UNARY_OPERATORS)
    return nodes.first().elementType
  }
}

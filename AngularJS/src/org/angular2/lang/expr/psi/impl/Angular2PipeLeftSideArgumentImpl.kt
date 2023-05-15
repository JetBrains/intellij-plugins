// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.impl.JSElementImpl
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2PipeLeftSideArgument

class Angular2PipeLeftSideArgumentImpl(elementType: IElementType?) : JSElementImpl(elementType), Angular2PipeLeftSideArgument {
  val pipeLeftSideExpression: JSExpression?
    get() = findChildByType(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS)
      ?.getPsi(JSExpression::class.java)

  override fun getArguments(): Array<JSExpression> {
    val leftExpr = pipeLeftSideExpression ?: return JSExpression.EMPTY_ARRAY
    val mainArgsList = pipeRightSideExpressions
    return if (mainArgsList == null) arrayOf(leftExpr) else ArrayUtil.prepend(leftExpr, mainArgsList)
  }

  private val pipeRightSideExpressions: Array<JSExpression>?
    get() = (parent as Angular2PipeExpressionImpl)
      .findChildByType(Angular2ElementTypes.PIPE_ARGUMENTS_LIST)
      ?.getPsi(Angular2PipeArgumentsListImpl::class.java)
      ?.pipeRightSideExpressions

  override fun hasSpreadElement(): Boolean {
    return false
  }
}
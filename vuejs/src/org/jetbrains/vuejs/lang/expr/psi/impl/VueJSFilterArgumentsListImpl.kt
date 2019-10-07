// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.impl.JSArgumentListImpl
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterArgumentsList

class VueJSFilterArgumentsListImpl(elementType: IElementType) : JSArgumentListImpl(elementType), VueJSFilterArgumentsList {

  internal val pipeRightSideExpressions: Array<JSExpression>
    get() = super.getArguments()

  private val pipeLeftSideExpression: JSExpression?
    get() = (parent as? VueJSFilterExpressionImpl)
      ?.findChildByType(VueJSElementTypes.FILTER_LEFT_SIDE_ARGUMENT)
      ?.getPsi(VueJSFilterLeftSideArgumentImpl::class.java)
      ?.pipeLeftSideExpression

  override fun getArguments(): Array<JSExpression> {
    val leftSide = pipeLeftSideExpression
    return if (leftSide != null)
      ArrayUtil.prepend(leftSide, super.getArguments())
    else
      JSExpression.EMPTY_ARRAY
  }

}

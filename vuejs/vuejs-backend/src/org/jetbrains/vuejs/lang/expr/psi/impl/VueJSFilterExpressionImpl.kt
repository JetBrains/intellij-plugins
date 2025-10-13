// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes
import org.jetbrains.vuejs.lang.expr.psi.VueJSElementVisitor
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterArgumentsList
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterExpression

class VueJSFilterExpressionImpl(elementType: IElementType)
  : JSExpressionImpl(elementType), VueJSFilterExpression, JSCallLikeExpressionCommon {

  private val leftSideArgument: VueJSFilterLeftSideArgumentImpl
    get() {
      return findChildByType(VueJSElementTypes.FILTER_LEFT_SIDE_ARGUMENT)
        ?.getPsi(VueJSFilterLeftSideArgumentImpl::class.java)!!
    }

  private val nameReference: JSReferenceExpression?
    get() = findPsiChildByType(VueJSElementTypes.FILTER_REFERENCE_EXPRESSION) as JSReferenceExpression?

  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is VueJSElementVisitor -> visitor.visitVueJSFilterExpression(this)
      is JSElementVisitor -> visitor.visitJSCallExpression(this)
      else -> super.accept(visitor)
    }
  }

  override fun getIndexingData(): JSElementIndexingData? = null

  override fun getName(): String? = nameReference?.referenceName

  override fun getMethodExpression(): JSExpression? = nameReference

  override fun getStubSafeMethodExpression(): JSExpression? = null

  override fun getArgumentList(): JSArgumentList = leftSideArgument

  override val filterArgumentsList: VueJSFilterArgumentsList? get() = leftSideArgument.pipeRightSideArguments

  override fun isRequireCall(): Boolean = false

  override fun isDefineCall(): Boolean = false

  override fun isElvis(): Boolean = false
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSForInStatementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression

class VueJSVForExpressionImpl(vueJSElementType: IElementType) : JSExpressionImpl(vueJSElementType), VueJSVForExpression {
  override fun getVarStatement(): JSVarStatement? {
    if (firstChild is JSVarStatement) return firstChild as JSVarStatement
    if (firstChild is JSParenthesizedExpression) {
      return PsiTreeUtil.findChildOfType(firstChild, JSVarStatement::class.java)
    }
    return null
  }

  override fun getReferenceExpression(): PsiElement? = children.firstOrNull { it is JSReferenceExpression }

  override fun getCollectionExpression(): JSExpression? {
    return JSForInStatementImpl.findCollectionExpression(this)
  }
}

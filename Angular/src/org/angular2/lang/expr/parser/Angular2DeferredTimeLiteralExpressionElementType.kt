// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSLiteralExpressionElementTypeImpl
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.psi.impl.Angular2DeferredTimeLiteralExpressionImpl

class Angular2DeferredTimeLiteralExpressionElementType : JSLiteralExpressionElementTypeImpl("NG:DEFERRED_TIME_LITERAL_EXPRESSION") {

  override fun construct(node: ASTNode): PsiElement? {
    return Angular2DeferredTimeLiteralExpressionImpl(node)
  }

  override fun getExternalId(): String {
    return debugName
  }

  override fun shouldCreateStub(node: ASTNode?): Boolean {
    return false
  }
}

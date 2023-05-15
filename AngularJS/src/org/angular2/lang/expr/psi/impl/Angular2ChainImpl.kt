// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.impl.JSStatementImpl
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.psi.Angular2Chain
import org.angular2.lang.expr.psi.Angular2ElementVisitor

class Angular2ChainImpl(elementType: IElementType?) : JSStatementImpl(elementType), Angular2Chain {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2Chain(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val statements: Array<JSExpressionStatement>
    get() {
      val nodes = getChildren(TokenSet.create(JSElementTypes.EXPRESSION_STATEMENT))
      if (nodes.isEmpty()) {
        return emptyArray()
      }
      return nodes.map { it.getPsi(JSExpressionStatement::class.java) }
        .toTypedArray()
    }
}
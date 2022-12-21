// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2Action
import org.angular2.lang.expr.psi.Angular2Chain
import org.angular2.lang.expr.psi.Angular2ElementVisitor

class Angular2ActionImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2Action {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2Action(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val statements: Array<JSExpressionStatement>
    get() {
      for (child in children) {
        if (child is Angular2Chain) {
          return child.statements
        }
        if (child is JSExpressionStatement) {
          return arrayOf(child)
        }
      }
      return emptyArray()
    }
}
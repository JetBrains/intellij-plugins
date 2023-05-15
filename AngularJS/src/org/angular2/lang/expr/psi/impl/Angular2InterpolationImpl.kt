// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Interpolation

class Angular2InterpolationImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2Interpolation {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2Interpolation(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val expression: JSExpression?
    get() = Angular2BindingImpl.getExpression(this)
}
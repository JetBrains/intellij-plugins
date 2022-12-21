// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Quote
import org.angular2.lang.expr.psi.Angular2SimpleBinding

class Angular2SimpleBindingImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2SimpleBinding {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2SimpleBinding(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val expression: JSExpression?
    get() = Angular2BindingImpl.getExpression(this)
  override val quote: Angular2Quote?
    get() = Angular2BindingImpl.getQuote(this)
}
// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.impl.Angular2BindingImpl.Companion.getExpression

class Angular2BlockParameterImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2BlockParameter {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2BlockParameter(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val expression: JSExpression?
    get() = getExpression(this)

  override val variable: JSVariable?
    get() = children.firstNotNullOfOrNull { it as? JSVarStatement }
      ?.variables?.firstOrNull()
}
// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey

class Angular2TemplateBindingKeyImpl(elementType: IElementType?) : JSExpressionImpl(elementType), Angular2TemplateBindingKey {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2TemplateBindingKey(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun getName(): String {
    return text
  }
}
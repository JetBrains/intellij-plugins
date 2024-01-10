// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings

class Angular2TemplateBindingsImpl(elementType: IElementType?, override val templateName: String)
  : Angular2EmbeddedExpressionImpl(elementType), Angular2TemplateBindings {

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2TemplateBindings(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val bindings: Array<Angular2TemplateBinding>
    get() = getChildren(TokenSet.create(Angular2ElementTypes.TEMPLATE_BINDING_STATEMENT))
      .mapNotNull { it.getPsi(Angular2TemplateBinding::class.java) }
      .toTypedArray()
}
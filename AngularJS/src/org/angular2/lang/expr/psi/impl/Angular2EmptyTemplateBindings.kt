// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings

class Angular2EmptyTemplateBindings(private val myParent: PsiElement?,
                                    override val templateName: String) : FakePsiElement(), Angular2TemplateBindings {

  override val bindings: Array<Angular2TemplateBinding>
    get() = Angular2TemplateBinding.EMPTY_ARRAY

  override fun getElementType(): IElementType {
    return Angular2ElementTypes.TEMPLATE_BINDINGS_STATEMENT
  }

  override fun getParent(): PsiElement? {
    return myParent
  }
}
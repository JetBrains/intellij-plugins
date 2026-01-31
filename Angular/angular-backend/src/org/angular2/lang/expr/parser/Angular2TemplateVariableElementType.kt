// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableImpl

internal class Angular2TemplateVariableElementType : JSVariableElementType("TEMPLATE_VARIABLE") {
  override fun toString(): String {
    return Angular2ElementTypes.EXTERNAL_ID_PREFIX + debugName
  }

  override fun construct(node: ASTNode): PsiElement {
    return Angular2TemplateVariableImpl(node)
  }
}
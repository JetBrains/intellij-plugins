// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression

class VueJSFilterReferenceExpressionImpl(
  elementType: IElementType,
) : JSReferenceExpressionImpl(elementType),
    VueJSFilterReferenceExpression {

  override fun isReferenceTo(element: PsiElement): Boolean {
    return if (element is JSFunction) {
      element == resolve()
    }
    else false
  }

  @Throws(IncorrectOperationException::class)
  override fun handleElementRename(newElementName: String): PsiElement {
    return handleElementRenameInternal(newElementName)
  }
}
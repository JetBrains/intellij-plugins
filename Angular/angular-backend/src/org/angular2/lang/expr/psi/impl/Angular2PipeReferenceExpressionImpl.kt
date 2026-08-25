// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.javascript.JSBuiltInTypeEngineEvaluation
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.angular2.entities.Angular2EntitiesProvider.isPipeTransformMethod
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class Angular2PipeReferenceExpressionImpl(elementType: IElementType?) : JSReferenceExpressionImpl(
  elementType), Angular2PipeReferenceExpression {
  override fun isReferenceTo(element: PsiElement): Boolean {
    return JSBuiltInTypeEngineEvaluation.performIsReferenceTo(this, element) {
      isPipeTransformMethod(element) && element == resolve()
    }
  }

  @Throws(IncorrectOperationException::class)
  override fun handleElementRename(newElementName: String): PsiElement {
    return handleElementRenameInternal(newElementName)
  }
}

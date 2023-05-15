// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSSuppressionHolder
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.impl.JSElementImpl
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.psi.HintedReferenceHost
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression

open class Angular2EmbeddedExpressionImpl(elementType: IElementType?)
  : JSElementImpl(elementType), JSSuppressionHolder, Angular2EmbeddedExpression, HintedReferenceHost {
  override fun getLanguage(): Language {
    return Angular2Language.INSTANCE
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is JSElementVisitor) {
      visitor.visitJSEmbeddedContent(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun allowTopLevelThis(): Boolean {
    return true
  }

  override fun getQuoteChar(): Char? {
    return JSEmbeddedContentImpl.getQuoteChar(this)
  }

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> {
    return PsiReference.EMPTY_ARRAY
  }

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean {
    return false
  }
}
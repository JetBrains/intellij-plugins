// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

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
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent

class VueJSEmbeddedExpressionContentImpl(elementType: IElementType) : JSElementImpl(elementType),
                                                                      JSSuppressionHolder, VueJSEmbeddedExpressionContent, HintedReferenceHost {

  override fun getLanguage(): Language {
    return elementType.language
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is JSElementVisitor) {
      visitor.visitJSEmbeddedContent(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun allowTopLevelThis(): Boolean {
    return true
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getQuoteChar(): Char? {
    return JSEmbeddedContentImpl.getQuoteChar(this)
  }

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> = PsiReference.EMPTY_ARRAY

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean = false

  override fun toString(): String {
    return super.toString() + "(${language.id})"
  }
}

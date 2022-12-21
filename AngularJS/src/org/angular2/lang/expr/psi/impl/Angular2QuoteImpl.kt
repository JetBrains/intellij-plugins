// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.impl.JSStatementImpl
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Quote

class Angular2QuoteImpl(elementType: IElementType?) : JSStatementImpl(elementType), Angular2Quote {
  override fun getName(): String {
    val node = findChildByType(JSKeywordSets.IDENTIFIER_NAMES)
    return node?.text ?: ""
  }

  override val contents: String
    get() {
      val colon = findChildByType(JSTokenTypes.COLON)
      return if (colon != null) this.text.substring(colon.startOffset - startOffset + 1) else ""
    }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2Quote(this)
    }
    else {
      super.accept(visitor)
    }
  }
}
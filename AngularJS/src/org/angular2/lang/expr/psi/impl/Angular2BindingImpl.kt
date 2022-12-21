// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2Quote

class Angular2BindingImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2Binding {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2Binding(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val expression: JSExpression?
    get() = getExpression(this)
  override val quote: Angular2Quote?
    get() = getQuote(this)

  companion object {
    fun getExpression(expression: Angular2EmbeddedExpressionImpl): JSExpression? {
      return expression.getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS)
        .firstNotNullOfOrNull { it.getPsi(JSExpression::class.java) }
    }

    fun getQuote(expression: Angular2EmbeddedExpressionImpl): Angular2Quote? {
      return expression.getChildren(TokenSet.create(Angular2ElementTypes.QUOTE_STATEMENT))
        .firstNotNullOfOrNull { it.getPsi(Angular2Quote::class.java) }
    }

    fun getEnclosingAttribute(expression: Angular2EmbeddedExpression): XmlAttribute? {
      var attribute = PsiTreeUtil.getParentOfType(expression, XmlAttribute::class.java)
      if (attribute == null) {
        attribute = PsiTreeUtil.getParentOfType(
          InjectedLanguageManager.getInstance(expression.project).getInjectionHost(expression),
          XmlAttribute::class.java)
      }
      return attribute
    }
  }
}
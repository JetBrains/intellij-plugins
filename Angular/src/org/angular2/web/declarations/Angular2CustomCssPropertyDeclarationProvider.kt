package org.angular2.web.declarations

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import org.angular2.web.scopes.Angular2CustomCssPropertiesScope

class Angular2CustomCssPropertyDeclarationProvider : WebSymbolDeclarationProvider {
  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> =
    when (element) {
      is XmlAttribute -> Angular2CustomCssPropertiesScope.createCustomCssProperty(element)
      is JSProperty -> Angular2CustomCssPropertiesScope.createCustomCssProperty(element)
      is JSLiteralExpression -> Angular2CustomCssPropertiesScope.createCustomCssProperty(element)
      else -> null
    }
      ?.declaration
      ?.let { listOf(it) } ?: emptyList()
}
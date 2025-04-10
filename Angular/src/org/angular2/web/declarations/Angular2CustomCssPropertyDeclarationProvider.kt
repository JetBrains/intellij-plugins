package org.angular2.web.declarations

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import org.angular2.web.scopes.Angular2CustomCssPropertiesInJsScope
import org.angular2.web.scopes.HtmlAttributesCustomCssPropertiesScope

class Angular2CustomCssPropertyDeclarationProvider : WebSymbolDeclarationProvider {
  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> =
    when (element) {
      is XmlAttribute -> HtmlAttributesCustomCssPropertiesScope.createCustomCssProperty(element)
      is JSProperty -> Angular2CustomCssPropertiesInJsScope.createCustomCssProperty(element)
      is JSLiteralExpression -> Angular2CustomCssPropertiesInJsScope.createCustomCssProperty(element)
      else -> null
    }
      ?.declaration
      ?.let { listOf(it) } ?: emptyList()
}
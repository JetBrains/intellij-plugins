package org.angular2.web

import com.intellij.javascript.webSymbols.jsKind
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.highlighting.WebSymbolHighlightingCustomizer
import com.intellij.webSymbols.js.WebSymbolJsKind
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding

class Angular2WebSymbolHighlightingCustomizer : WebSymbolHighlightingCustomizer {

  override fun getSymbolKindTextAttributes(qualifiedKind: WebSymbolQualifiedKind): TextAttributesKey? =
    when (qualifiedKind) {
      NG_DIRECTIVE_INPUTS -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      NG_DIRECTIVE_OUTPUTS -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
      NG_DIRECTIVE_ATTRIBUTE_SELECTORS, NG_DIRECTIVE_ONE_TIME_BINDINGS -> XmlHighlighterColors.HTML_ATTRIBUTE_NAME
      NG_DIRECTIVE_ELEMENT_SELECTORS -> XmlHighlighterColors.HTML_TAG_NAME
      else -> null
    }

  override fun getDefaultHostClassTextAttributes(): Map<Class<out PsiExternalReferenceHost>, TextAttributesKey> =
    mapOf(
      Angular2HtmlPropertyBinding::class.java to Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME,
      Angular2HtmlEvent::class.java to Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME,
    )

  override fun getSymbolTextAttributes(host: PsiExternalReferenceHost, symbol: WebSymbol, level: Int): TextAttributesKey? {
    when (symbol.qualifiedKind) {
      WebSymbol.JS_SYMBOLS ->
        if (symbol.jsKind == WebSymbolJsKind.Variable
            && symbol is PsiSourcedWebSymbol && symbol.source?.language == Angular2Language)
          return Angular2HighlighterColors.NG_VARIABLE
      WebSymbol.HTML_ATTRIBUTES ->
        if (host is JSProperty && isHostBinding(host)) {
          val info = Angular2AttributeNameParser.parse(symbol.name)
          return when (info.type) {
            Angular2AttributeType.PROPERTY_BINDING -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
            Angular2AttributeType.EVENT -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
            else -> null
          }
        } else if (host is Angular2HtmlPropertyBinding && level == 0) {
          return Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
        } else if (host is Angular2HtmlEvent && level == 0) {
          return Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
        }
      NG_PROPERTY_BINDINGS -> if (host is JSLiteralExpression) {
        return Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      }
      NG_EVENT_BINDINGS -> if (host is JSLiteralExpression) {
        return Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
      }
    }
    return null
  }

}
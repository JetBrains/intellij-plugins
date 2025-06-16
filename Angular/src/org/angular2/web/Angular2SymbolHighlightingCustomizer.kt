package org.angular2.web

import com.intellij.javascript.polySymbols.jsKind
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.highlighting.PolySymbolHighlightingCustomizer
import com.intellij.polySymbols.js.JsSymbolSymbolKind
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding

class Angular2SymbolHighlightingCustomizer : PolySymbolHighlightingCustomizer {

  override fun getSymbolKindTextAttributes(qualifiedKind: PolySymbolQualifiedKind): TextAttributesKey? =
    when (qualifiedKind) {
      NG_DIRECTIVE_INPUTS -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      NG_DIRECTIVE_OUTPUTS -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
      NG_DIRECTIVE_IN_OUTS -> Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME
      NG_DIRECTIVE_ATTRIBUTE_SELECTORS, NG_DIRECTIVE_ONE_TIME_BINDINGS -> XmlHighlighterColors.HTML_ATTRIBUTE_NAME
      NG_DIRECTIVE_ELEMENT_SELECTORS -> XmlHighlighterColors.HTML_TAG_NAME
      else -> null
    }

  override fun getDefaultHostClassTextAttributes(): Map<Class<out PsiExternalReferenceHost>, TextAttributesKey> =
    mapOf(
      Angular2HtmlPropertyBinding::class.java to Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME,
      Angular2HtmlBananaBoxBinding::class.java to Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME,
      Angular2HtmlEvent::class.java to Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME,
    )

  override fun getSymbolTextAttributes(host: PsiExternalReferenceHost, symbol: PolySymbol, level: Int): TextAttributesKey? {
    when (symbol.qualifiedKind) {
      JS_SYMBOLS ->
        if (symbol.jsKind == JsSymbolSymbolKind.Variable
            && symbol is PsiSourcedPolySymbol && symbol.source?.language == Angular2Language)
          return Angular2HighlighterColors.NG_VARIABLE
      HTML_ATTRIBUTES ->
        if (host is JSProperty && isHostBinding(host)) {
          val info = Angular2AttributeNameParser.parse(symbol.name)
          return when (info.type) {
            Angular2AttributeType.PROPERTY_BINDING -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
            Angular2AttributeType.EVENT -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
            else -> null
          }
        }
        else if (level == 0)
          return when (host) {
            is Angular2HtmlPropertyBinding -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
            is Angular2HtmlEvent -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
            is Angular2HtmlBananaBoxBinding -> Angular2HtmlHighlighterColors.NG_BANANA_BINDING_ATTR_NAME
            else -> null
          }
      NG_PROPERTY_BINDINGS -> if (host is JSLiteralExpression) {
        return Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      }
      NG_DIRECTIVE_ATTRIBUTES -> if (host is JSLiteralExpression) {
        return XmlHighlighterColors.HTML_ATTRIBUTE_NAME
      }
      NG_EVENT_BINDINGS -> if (host is JSLiteralExpression) {
        return Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
      }
    }
    return null
  }

}
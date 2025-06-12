package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.query.PolySymbolsQueryResultsCustomizer
import com.intellij.polySymbols.query.PolySymbolsQueryResultsCustomizerFactory
import com.intellij.psi.PsiElement
import org.angular2.Angular2Framework
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2SymbolDelegate
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS

object Angular2FormsPolySymbolQueryResultsCustomizer : PolySymbolsQueryResultsCustomizer {

  override fun createPointer(): Pointer<out PolySymbolsQueryResultsCustomizer> =
    Pointer.hardPointer(this)

  override fun apply(matches: List<PolySymbol>, strict: Boolean, qualifiedName: PolySymbolQualifiedName): List<PolySymbol> =
    if (qualifiedName.qualifiedKind == NG_DIRECTIVE_ATTRIBUTE_SELECTORS
        && (qualifiedName.name in FORM_ANY_CONTROL_NAME_ATTRIBUTES))
      matches.map { it.remapFormControlNameSymbol() }
    else
      matches

  override fun apply(item: PolySymbolCodeCompletionItem, qualifiedKind: PolySymbolQualifiedKind): PolySymbolCodeCompletionItem? =
    item

  override fun getModificationCount(): Long = 0

  class Factory : PolySymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: PolyContext): PolySymbolsQueryResultsCustomizer? =
      if (context.framework == Angular2Framework.ID && location.containingFile is Angular2HtmlFile)
        Angular2FormsPolySymbolQueryResultsCustomizer
      else
        null
  }

  private fun PolySymbol.remapFormControlNameSymbol(): PolySymbol =
    if (this is Angular2DirectiveSymbolWrapper)
      Angular2FormControlAttributeWrapper(this)
    else
      this

  private class Angular2FormControlAttributeWrapper(
    delegate: Angular2DirectiveSymbolWrapper,
  ) : Angular2SymbolDelegate<Angular2DirectiveSymbolWrapper>(delegate) {

    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(
          PolySymbolHtmlAttributeValue.create(
            PolySymbolHtmlAttributeValue.Kind.PLAIN,
            PolySymbolHtmlAttributeValue.Type.SYMBOL,
            required = true,
          )
        )
        else -> super[property]
      }

    override fun isEquivalentTo(symbol: Symbol): Boolean {
      return this == symbol || delegate.isEquivalentTo(symbol)
    }

    override fun createPointer(): Pointer<Angular2FormControlAttributeWrapper> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { Angular2FormControlAttributeWrapper(it) }
      }
    }
  }
}

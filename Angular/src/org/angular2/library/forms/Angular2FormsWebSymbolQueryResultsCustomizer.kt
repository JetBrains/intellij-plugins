package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.PolySymbol
import com.intellij.webSymbols.PolySymbolQualifiedKind
import com.intellij.webSymbols.PolySymbolQualifiedName
import com.intellij.webSymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.webSymbols.context.PolyContext
import com.intellij.webSymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import org.angular2.Angular2Framework
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2SymbolDelegate
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS

object Angular2FormsWebSymbolQueryResultsCustomizer : WebSymbolsQueryResultsCustomizer {

  override fun createPointer(): Pointer<out WebSymbolsQueryResultsCustomizer> =
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

  class Factory : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: PolyContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == Angular2Framework.ID && location.containingFile is Angular2HtmlFile)
        Angular2FormsWebSymbolQueryResultsCustomizer
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
    override val attributeValue: PolySymbolHtmlAttributeValue?
      get() = PolySymbolHtmlAttributeValue.create(
        PolySymbolHtmlAttributeValue.Kind.PLAIN,
        PolySymbolHtmlAttributeValue.Type.SYMBOL,
        required = true,
      )

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

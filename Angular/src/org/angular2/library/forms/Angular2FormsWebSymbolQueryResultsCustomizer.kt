package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
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

  override fun apply(matches: List<WebSymbol>, strict: Boolean, qualifiedName: WebSymbolQualifiedName): List<WebSymbol> =
    if (qualifiedName.qualifiedKind == NG_DIRECTIVE_ATTRIBUTE_SELECTORS
        && (qualifiedName.name in FORM_ANY_CONTROL_NAME_ATTRIBUTES))
      matches.map { it.remapFormControlNameSymbol() }
    else
      matches

  override fun apply(item: WebSymbolCodeCompletionItem, qualifiedKind: WebSymbolQualifiedKind): WebSymbolCodeCompletionItem? =
    item

  override fun getModificationCount(): Long = 0

  class Factory : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: WebSymbolsContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == Angular2Framework.ID && location.containingFile is Angular2HtmlFile)
        Angular2FormsWebSymbolQueryResultsCustomizer
      else
        null
  }

  private fun WebSymbol.remapFormControlNameSymbol(): WebSymbol =
    if (this is Angular2DirectiveSymbolWrapper)
      Angular2FormControlAttributeWrapper(this)
    else
      this

  private class Angular2FormControlAttributeWrapper(
    delegate: Angular2DirectiveSymbolWrapper,
  ) : Angular2SymbolDelegate<Angular2DirectiveSymbolWrapper>(delegate) {
    override val attributeValue: WebSymbolHtmlAttributeValue?
      get() = WebSymbolHtmlAttributeValue.create(
        WebSymbolHtmlAttributeValue.Kind.PLAIN,
        WebSymbolHtmlAttributeValue.Type.SYMBOL,
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

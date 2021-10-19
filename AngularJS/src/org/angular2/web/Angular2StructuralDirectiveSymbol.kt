package org.angular2.web

import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolHtmlAttributeValueData
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.model.Pointer
import org.angular2.entities.Angular2Directive

class Angular2StructuralDirectiveSymbol(private val directive: Angular2Directive,
                                        sourceSymbol: Angular2Symbol,
                                        private val hasInputsToBind: Boolean) :
  Angular2SymbolDelegate<Angular2Symbol>(sourceSymbol) {

  override val attributeValue: WebSymbol.AttributeValue?
    get() = if (!hasInputsToBind)
      WebSymbolHtmlAttributeValueData(required = false)
    else super.attributeValue

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.HIGH

  override val namespace: WebSymbolsContainer.Namespace
    get() = WebSymbolsContainer.Namespace.JS

  override val kind: SymbolKind
    get() = Angular2WebSymbolsAdditionalContextProvider.KIND_NG_STRUCTURAL_DIRECTIVES

  override val properties: Map<String, Any>
    get() = super.properties + Pair(Angular2WebSymbolsAdditionalContextProvider.PROP_SYMBOL_DIRECTIVE, directive)

  override fun createPointer(): Pointer<Angular2StructuralDirectiveSymbol> {
    val directivePtr = directive.createPointer()
    val selectorPtr = delegate.createPointer()
    val hasInputsToBind = this.hasInputsToBind
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val selector = selectorPtr.dereference() ?: return@Pointer null
      Angular2StructuralDirectiveSymbol(directive, selector, hasInputsToBind)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is Angular2StructuralDirectiveSymbol
    && other.delegate == delegate

  override fun hashCode(): Int =
    delegate.hashCode()

}
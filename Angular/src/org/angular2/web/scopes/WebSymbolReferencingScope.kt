package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import com.intellij.webSymbols.utils.qualifiedKind

class WebSymbolReferencingScope(
  qualifiedKind: WebSymbolQualifiedKind,
  name: String,
  private val isExclusive: Boolean,
  origin: WebSymbolOrigin,
  vararg qualifiedKinds: WebSymbolQualifiedKind,
) : WebSymbolsScope {

  private val symbol = ReferencingWebSymbol.create(
    qualifiedKind, name, origin, *qualifiedKinds
  )

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    isExclusive && qualifiedKind == symbol.qualifiedKind

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (qualifiedKind == symbol.qualifiedKind)
      listOf(symbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScope> =
    hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is WebSymbolReferencingScope
    && other.symbol == symbol

  override fun hashCode(): Int =
    symbol.hashCode()
}
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.PolySymbolQualifiedKind
import com.intellij.webSymbols.PolySymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.utils.ReferencingPolySymbol
import com.intellij.webSymbols.utils.qualifiedKind

class PolySymbolReferencingScope(
  qualifiedKind: PolySymbolQualifiedKind,
  name: String,
  private val isExclusive: Boolean,
  origin: WebSymbolOrigin,
  vararg qualifiedKinds: PolySymbolQualifiedKind,
) : PolySymbolsScope {

  private val symbol = ReferencingPolySymbol.create(
    qualifiedKind, name, origin, *qualifiedKinds
  )

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    isExclusive && qualifiedKind == symbol.qualifiedKind

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbolsScope> =
    if (qualifiedKind == symbol.qualifiedKind)
      listOf(symbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out PolySymbolsScope> =
    hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is PolySymbolReferencingScope
    && other.symbol == symbol

  override fun hashCode(): Int =
    symbol.hashCode()
}
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol

class PolySymbolReferencingScope(
  kind: PolySymbolKind,
  name: String,
  private val isExclusive: Boolean,
  origin: PolySymbolOrigin,
  vararg kinds: PolySymbolKind,
) : PolySymbolScope {

  private val symbol = ReferencingPolySymbol.create(
    kind, name, origin, *kinds
  )

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    isExclusive && kind == symbol.kind

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (kind == symbol.kind)
      listOf(symbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out PolySymbolScope> =
    hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is PolySymbolReferencingScope
    && other.symbol == symbol

  override fun hashCode(): Int =
    symbol.hashCode()
}
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol

class PolySymbolReferencingScope(
  qualifiedKind: PolySymbolQualifiedKind,
  name: String,
  private val isExclusive: Boolean,
  origin: PolySymbolOrigin,
  vararg qualifiedKinds: PolySymbolQualifiedKind,
) : PolySymbolScope {

  private val symbol = ReferencingPolySymbol.create(
    qualifiedKind, name, origin, *qualifiedKinds
  )

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    isExclusive && qualifiedKind == symbol.qualifiedKind

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (qualifiedKind == symbol.qualifiedKind)
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
package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormControl : PolySymbolWithPattern, Angular2Symbol {

  override val name: @NlsSafe String
    get() = "Unknown form control"

  override val pattern: PolySymbolPattern =
    PolySymbolPatternFactory.createRegExMatch(".*")

  override val kind: PolySymbolKind
    get() = NG_FORM_CONTROL_PROPS

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOWEST

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PolySymbol.PROP_HIDE_FROM_COMPLETION -> true as T
      PolySymbol.PROP_DOC_HIDE_PATTERN -> true as T
      else -> super<Angular2Symbol>.get(property)
    }

  override fun createPointer(): Pointer<Angular2UnknownFormControl> =
    Pointer.hardPointer(this)
}
package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2FormArrayControl : PolySymbolWithPattern {

  override val name: @NlsSafe String
    get() = "Form array control"

  override val pattern: PolySymbolsPattern =
    PolySymbolsPatternFactory.createRegExMatch("[0-9]+")

  override val qualifiedKind: PolySymbolQualifiedKind
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
      else -> null
    }

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)
}
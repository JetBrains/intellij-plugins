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
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.util.containers.Stack
import org.angular2.library.forms.NG_FORM_ARRAY_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormArray : PolySymbolWithPattern, PolySymbolsScope {

  override val name: @NlsSafe String
    get() = "Unknown form array"

  override val pattern: PolySymbolsPattern =
    PolySymbolsPatternFactory.createRegExMatch(".*")

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: PolySymbolsListSymbolsQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbol> =
    if (qualifiedKind == NG_FORM_CONTROL_PROPS)
      listOf(Angular2FormArrayControl)
    else
      emptyList()

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = NG_FORM_ARRAY_PROPS

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOWEST

  override fun getModificationCount(): Long = 0

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PolySymbol.PROP_HIDE_FROM_COMPLETION -> true as T
      PolySymbol.PROP_DOC_HIDE_PATTERN -> true as T
      else -> null
    }

  override fun createPointer(): Pointer<out Angular2UnknownFormArray> =
    Pointer.hardPointer(this)
}
package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol.DocHidePatternProperty
import com.intellij.polySymbols.PolySymbol.HideFromCompletionProperty
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.angular2.library.forms.NG_FORM_ARRAY_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2Symbol

object Angular2UnknownFormArray : PolySymbolWithPattern, PolySymbolScope, Angular2Symbol {

  override val name: @NlsSafe String
    get() = "Unknown form array"

  override val pattern: PolySymbolPattern =
    PolySymbolPatternFactory.createRegExMatch(".*")

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (kind == NG_FORM_CONTROL_PROPS)
      listOf(Angular2FormArrayControl)
    else
      emptyList()

  override val kind: PolySymbolKind
    get() = NG_FORM_ARRAY_PROPS

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  override fun getModificationCount(): Long = 0

  @PolySymbol.Property(HideFromCompletionProperty::class)
  val hideFromCompletion: Boolean
    get() = true

  @PolySymbol.Property(DocHidePatternProperty::class)
  val docHidePattern: Boolean
    get() = true

  override fun createPointer(): Pointer<out Angular2UnknownFormArray> =
    Pointer.hardPointer(this)
}
package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol.DocHidePatternProperty
import com.intellij.polySymbols.PolySymbol.HideFromCompletionProperty
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2Symbol

object Angular2UnknownFormControl : PolySymbolWithPattern, Angular2Symbol {

  override val name: @NlsSafe String
    get() = "Unknown form control"

  override val pattern: PolySymbolPattern =
    PolySymbolPatternFactory.createRegExMatch(".*")

  override val kind: PolySymbolKind
    get() = NG_FORM_CONTROL_PROPS

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  @PolySymbol.Property(HideFromCompletionProperty::class)
  val hideFromCompletion: Boolean
    get() = true

  @PolySymbol.Property(DocHidePatternProperty::class)
  val docHidePattern: Boolean
    get() = true

  override fun createPointer(): Pointer<Angular2UnknownFormControl> =
    Pointer.hardPointer(this)
}
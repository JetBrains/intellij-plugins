package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.SymbolKind
import com.intellij.polySymbols.SymbolNamespace
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormControl : PolySymbol {

  override val name: @NlsSafe String
    get() = "Unknown form control"

  override val pattern: PolySymbolsPattern? = PolySymbolsPatternFactory.createRegExMatch(".*")

  override val namespace: @NlsSafe SymbolNamespace
    get() = PolySymbol.Companion.NAMESPACE_JS

  override val kind: @NlsSafe SymbolKind
    get() = NG_FORM_CONTROL_PROPS.kind

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION to true,
          PolySymbol.Companion.PROP_DOC_HIDE_PATTERN to true)

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)
}
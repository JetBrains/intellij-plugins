package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_FIELDS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormGroup : WebSymbol {

  override val name: @NlsSafe String
    get() = "Unknown form group"

  override val pattern: WebSymbolsPattern? = WebSymbolsPatternFactory.createRegExMatch(".*")

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    when (qualifiedKind) {
      NG_FORM_CONTROL_PROPS -> listOf(Angular2UnknownFormControl)
      NG_FORM_GROUP_FIELDS -> listOf(Angular2UnknownFormArray)
      NG_FORM_GROUP_PROPS -> listOf(this)
      else -> emptyList()
    }

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind in NG_FORM_ANY_CONTROL_PROPS

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(WebSymbol.Companion.PROP_HIDE_FROM_COMPLETION to true,
          WebSymbol.Companion.PROP_DOC_HIDE_PATTERN to true)

  override val namespace: @NlsSafe SymbolNamespace
    get() = WebSymbol.Companion.NAMESPACE_JS

  override val kind: @NlsSafe SymbolKind
    get() = NG_FORM_GROUP_PROPS.kind

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override fun createPointer(): Pointer<out WebSymbol> =
    Pointer.hardPointer(this)
}
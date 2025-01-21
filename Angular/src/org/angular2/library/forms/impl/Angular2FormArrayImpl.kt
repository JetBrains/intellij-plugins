package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import org.angular2.library.forms.Angular2FormArray
import org.angular2.library.forms.NG_FORM_ARRAY_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS

class Angular2FormArrayImpl(
  source: JSProperty,
) : Angular2FormArray, Angular2FormAbstractControlImpl(source) {

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = NG_FORM_ARRAY_PROPS

  override fun getSymbols(
    qualifiedKind: WebSymbolQualifiedKind,
    params: WebSymbolsListSymbolsQueryParams,
    scope: Stack<WebSymbolsScope>,
  ): List<WebSymbolsScope> =
    if (qualifiedKind == NG_FORM_CONTROL_PROPS)
      listOf(Angular2FormArrayControl)
    else
      emptyList()

  override fun createPointer(): Pointer<Angular2FormArrayImpl> {
    val sourcePtr = (source as JSProperty).createSmartPointer()
    return Pointer {
      sourcePtr.element?.let { Angular2FormArrayImpl(it) }
    }
  }


}
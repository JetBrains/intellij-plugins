package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.angular2.library.forms.Angular2FormControl
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS

class Angular2FormControlImpl(
  source: JSProperty,
) : Angular2FormControl, Angular2FormAbstractControlImpl(source) {

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = NG_FORM_CONTROL_PROPS

  override fun createPointer(): Pointer<Angular2FormControlImpl> {
    val sourcePtr = (source as JSProperty).createSmartPointer()
    return Pointer {
      sourcePtr.element?.let { Angular2FormControlImpl(it) }
    }
  }
}
package org.angular2.library.forms.impl

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.psi.createSmartPointer
import org.angular2.library.forms.Angular2FormControl
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS

class Angular2FormControlImpl(
  source: JSProperty,
) : Angular2FormControl, Angular2FormAbstractControlImpl(source) {

  override val kind: PolySymbolKind
    get() = NG_FORM_CONTROL_PROPS

  override fun createPointer(): Pointer<Angular2FormControlImpl> {
    val sourcePtr = (source as JSProperty).createSmartPointer()
    return Pointer {
      sourcePtr.element?.let { Angular2FormControlImpl(it) }
    }
  }
}
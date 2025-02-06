package org.angular2.library.forms

import com.intellij.model.Pointer
import org.angular2.web.Angular2PsiSourcedSymbol

interface Angular2FormAbstractControl : Angular2PsiSourcedSymbol {
  override fun createPointer(): Pointer<out Angular2FormAbstractControl>
}
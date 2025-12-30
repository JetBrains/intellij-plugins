package org.angular2.library.forms

import com.intellij.model.Pointer

interface Angular2FormControl : Angular2FormAbstractControl {
  override fun createPointer(): Pointer<out Angular2FormControl>
}
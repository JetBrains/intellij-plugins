package org.angular2.library.forms

import com.intellij.model.Pointer

interface Angular2FormGroup : Angular2FormAbstractControl {
  val members: List<Angular2FormAbstractControl>
  override fun createPointer(): Pointer<out Angular2FormGroup>
}
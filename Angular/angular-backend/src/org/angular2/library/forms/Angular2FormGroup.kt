package org.angular2.library.forms

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement

interface Angular2FormGroup : Angular2FormAbstractControl {
  override val source: PsiElement
  val members: List<Angular2FormAbstractControl>
  val initializer: JSObjectLiteralExpression?
  override fun createPointer(): Pointer<out Angular2FormGroup>
}
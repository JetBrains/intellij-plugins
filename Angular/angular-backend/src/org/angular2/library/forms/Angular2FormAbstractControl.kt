package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement
import org.angular2.web.Angular2PsiLinkedSymbol

interface Angular2FormAbstractControl : Angular2PsiLinkedSymbol, PolySymbolScope {
  override fun createPointer(): Pointer<out Angular2FormAbstractControl>
  override val source: PsiElement
}
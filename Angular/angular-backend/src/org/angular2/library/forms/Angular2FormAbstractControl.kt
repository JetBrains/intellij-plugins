package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.openapi.util.ModificationTracker
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.web.Angular2PsiSourcedSymbol

interface Angular2FormAbstractControl : Angular2PsiSourcedSymbol, PolySymbolScope {
  override fun createPointer(): Pointer<out Angular2FormAbstractControl>
  override val source: PsiElement
  override val modificationTracker: ModificationTracker
    get() = PsiModificationTracker.getInstance(source.project)
}
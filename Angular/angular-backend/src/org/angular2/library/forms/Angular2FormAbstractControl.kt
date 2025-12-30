package org.angular2.library.forms

import com.intellij.model.Pointer
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.web.Angular2PsiSourcedSymbol

interface Angular2FormAbstractControl : Angular2PsiSourcedSymbol, PolySymbolScope {
  override fun createPointer(): Pointer<out Angular2FormAbstractControl>
  override fun getModificationCount(): Long =
    source?.let { PsiModificationTracker.getInstance(it.project).modificationCount } ?: 0
}
// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.asPolySymbolPriority

data class VueTypedDirectiveModifier(
  override val name: String,
  override val source: PsiElement?,
) : VueDirectiveModifier, PsiSourcedPolySymbol {
  override fun createPointer(): Pointer<out VueTypedDirectiveModifier> {
    val name = this.name
    val sourcePointer = source?.createSmartPointer()
    return Pointer {
      val source = sourcePointer?.let { it.dereference() ?: return@Pointer null }
      VueTypedDirectiveModifier(name, source)
    }
  }
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.model.resolveInjectionSymbol
import org.jetbrains.vuejs.types.VueSourceProvideType

data class VueSourceProvide(
  override val name: String,
  private val sourceElement: PsiElement,
  private val symbolSource: PsiNamedElement? = null,
) : VueProvide, PsiSourcedPolySymbol {

  override val injectionKey: PsiNamedElement?
    get() = resolveInjectionSymbol(symbolSource)

  override val type: JSType = VueSourceProvideType(sourceElement, symbolSource)

  override val source: PsiElement = when (sourceElement) {
    is JSLiteralExpression -> VueImplicitElement(name, type, sourceElement, JSImplicitElement.Type.Property, true)
    else -> sourceElement
  }

  override fun createPointer(): Pointer<VueSourceProvide> {
    val name = name
    val sourcePtr = sourceElement.createSmartPointer()
    val symbolSourcePtr = symbolSource?.createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      val symbolSource = symbolSourcePtr?.let { it.dereference() ?: return@Pointer null }
      VueSourceProvide(name, source, symbolSource)
    }
  }
}
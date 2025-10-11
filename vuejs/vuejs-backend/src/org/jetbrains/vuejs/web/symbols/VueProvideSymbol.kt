// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.psi.PsiNamedElement
import com.intellij.util.asSafely
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.web.VUE_PROVIDES

class VueProvideSymbol(
  private val provide: VueProvide,
  private val owner: VueContainer,
  override val origin: PolySymbolOrigin,
) : VueDocumentedItemSymbol<VueProvide>(provide.name, provide) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_PROVIDES

  override val type: JSType?
    get() = item.jsType

  val injectionKey: PsiNamedElement?
    get() = provide.injectionKey

  override fun createPointer(): Pointer<VueProvideSymbol> = object : Pointer<VueProvideSymbol> {
    private val name = this@VueProvideSymbol.name
    private val origin = this@VueProvideSymbol.origin
    private val pointer = this@VueProvideSymbol.owner.createPointer()

    override fun dereference(): VueProvideSymbol? =
      pointer.dereference()?.asSafely<VueContainer>()?.let { container ->
        container.provides.find { it.name == name }?.let { provide ->
          VueProvideSymbol(provide, container, origin)
        }
      }
  }
}
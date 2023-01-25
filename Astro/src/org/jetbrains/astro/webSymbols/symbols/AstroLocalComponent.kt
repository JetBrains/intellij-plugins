// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.*
import org.jetbrains.astro.webSymbols.AstroProximity
import org.jetbrains.astro.webSymbols.AstroQueryConfigurator

class AstroLocalComponent(override val name: String,
                          override val source: PsiElement,
                          override val priority: WebSymbol.Priority = WebSymbol.Priority.HIGH) : PsiSourcedWebSymbol {

  override val origin: WebSymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = WebSymbol.NAMESPACE_HTML

  override val kind: SymbolKind
    get() = WebSymbol.KIND_HTML_ELEMENTS

  override val properties: Map<String, Any>
    get() = mapOf(Pair(AstroQueryConfigurator.PROP_ASTRO_PROXIMITY, AstroProximity.LOCAL))

  override fun createPointer(): Pointer<out WebSymbol> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroLocalComponent(name, it) }
    }
  }

}
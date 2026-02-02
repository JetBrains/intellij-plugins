// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.PolySymbolIsolatedMappingScope
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_SLOTS_PROP

/**
 * [Details](https://github.com/vuejs/core/releases/tag/v3.6.0-alpha.1#about-vapor-mode:~:text=Implicit%20instance%20properties%20like%20%24slots%20and%20%24props%20are%20not%20available%20in%20Vapor%20template%20expressions)
 */
private val VAPOR_EXCLUDED_PROPERTIES = setOf(
  INSTANCE_SLOTS_PROP,
  INSTANCE_PROPS_PROP,
)

internal class VueContainerSymbolScopeProvider : VueTemplateSymbolScopesProvider {
  override fun getScopes(
    element: PsiElement,
    hostElement: PsiElement?,
  ): List<PolySymbolScope> =
    listOf(VueContainerScope(hostElement ?: element))

  private class VueContainerScope(
    location: PsiElement,
  ) : PolySymbolIsolatedMappingScope<PsiElement>(mapOf(JS_SYMBOLS to JS_PROPERTIES), location) {

    private val excludedProperties by lazy {
      if (VueModelManager.findEnclosingContainer(location).let { it is VueComponent && it.mode == VueMode.VAPOR })
        VAPOR_EXCLUDED_PROPERTIES
      else
        emptySet()
    }

    override fun acceptSymbol(symbol: PolySymbol): Boolean =
      symbol.name !in excludedProperties

    override val subScopeBuilder: (PolySymbolQueryExecutor, PsiElement) -> List<PolySymbolScope>
      get() = { _, location ->
        listOfNotNull(VueModelManager.findEnclosingContainer(location).instanceScope)
      }

    override fun createPointer(): Pointer<VueContainerScope> {
      val locationPtr = location.createSmartPointer()
      return Pointer {
        VueContainerScope(locationPtr.dereference() ?: return@Pointer null)
      }
    }
  }
}

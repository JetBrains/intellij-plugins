// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScope
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.astro.polySymbols.AstroProximity
import org.jetbrains.astro.polySymbols.AstroProximityProperty
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_PROPS

// Currently, we don't support detection of props for components of other UI frameworks and use this
// symbol as a wildcard for all components that aren't from Astro. Once we implement an extension point
// for other frameworks to contribute Astro symbols, the logic of how we handle this will change.

class UiFrameworkComponent(
  override val name: String,
  override val source: PsiElement,
  override val priority: PolySymbol.Priority = PolySymbol.Priority.HIGH,
) : ComponentPolySymbol, PolySymbolScope by polySymbolScope(
  {
    provides(UI_FRAMEWORK_COMPONENT_PROPS)
    filterNameMatches { qualifiedName, matches ->
      if (qualifiedName.matches(HTML_ATTRIBUTES) && name.contains(":"))
        emptyList()
      else
        matches
    }
    initialize {
      add(AstroComponentWildcardAttribute)
    }
  }) {

  override val kind: PolySymbolKind
    get() = UI_FRAMEWORK_COMPONENTS

  @PolySymbol.Property(AstroProximityProperty::class)
  val astroProximity: AstroProximity
    get() = AstroProximity.LOCAL

  override fun createPointer(): Pointer<out UiFrameworkComponent> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { UiFrameworkComponent(name, it) }
    }
  }
}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScopeCached
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.resolveIfImportSpecifier
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.polySymbols.symbols.AstroLocalComponent
import org.jetbrains.astro.polySymbols.symbols.UiFrameworkComponent

internal fun astroFrontmatterScope(file: AstroFileImpl): PolySymbolScope =
  polySymbolScopeCached(file) {
    provides(ASTRO_COMPONENTS, UI_FRAMEWORK_COMPONENTS)
    initialize {
      cacheDependencies(element)
      // Self reference
      add(AstroLocalComponent("Astro.self", element, PolySymbol.Priority.NORMAL))
      element.astroContentRoot()?.frontmatterScript()?.let {
        JSStubBasedPsiTreeUtil.processDeclarationsInScope(it, { element, _ ->
          val namedElement = element as? JSPsiNamedElementBase
          val name = namedElement?.name
          if (name?.getOrNull(0)?.isUpperCase() != true) return@processDeclarationsInScope true
          if (namedElement !is JSClass) {
            val symbol = when (namedElement.resolveIfImportSpecifier().containingFile) {
              is AstroFileImpl -> AstroLocalComponent(name, namedElement)
              // TODO: Introduce extension point for frameworks to contribute their symbols for Astro.
              else -> UiFrameworkComponent(name, namedElement)
            }
            add(symbol)
          }
          true
        }, false)
      }
    }
  }
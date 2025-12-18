// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.resolveIfImportSpecifier
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.polySymbols.symbols.AstroLocalComponent
import org.jetbrains.astro.polySymbols.symbols.UiFrameworkComponent

class AstroFrontmatterScope(val file: AstroFileImpl) :
  PolySymbolScopeWithCache<AstroFileImpl, Unit>(AstroFramework.ID, file.project, file, Unit) {

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == ASTRO_COMPONENTS || kind == UI_FRAMEWORK_COMPONENTS

  override fun createPointer(): Pointer<AstroFrontmatterScope> {
    val filePtr = file.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { AstroFrontmatterScope(it) }
    }
  }

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    file.astroContentRoot()?.frontmatterScript()?.let {
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
          consumer(symbol)
        }
        true
      }, false)
    }
    // Self reference
    consumer(AstroLocalComponent("Astro.self", file, PolySymbol.Priority.NORMAL))
    cacheDependencies.add(file)
  }

}
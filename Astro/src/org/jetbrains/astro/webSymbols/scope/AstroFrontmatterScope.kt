// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.resolveIfImportSpecifier
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.webSymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.webSymbols.symbols.AstroLocalComponent
import org.jetbrains.astro.webSymbols.symbols.UiFrameworkComponent

class AstroFrontmatterScope(val file: AstroFileImpl)
  : WebSymbolsScopeWithCache<AstroFileImpl, Unit>(AstroFramework.ID, file.project, file, Unit) {

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == ASTRO_COMPONENTS || qualifiedKind == UI_FRAMEWORK_COMPONENTS

  override fun createPointer(): Pointer<AstroFrontmatterScope> {
    val filePtr = file.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { AstroFrontmatterScope(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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
    consumer(AstroLocalComponent("Astro.self", file, WebSymbol.Priority.NORMAL))
    cacheDependencies.add(file)
  }

}
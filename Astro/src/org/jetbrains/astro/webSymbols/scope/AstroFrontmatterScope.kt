// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.symbols.AstroLocalComponent

class AstroFrontmatterScope(val file: AstroFileImpl)
  : WebSymbolsScopeWithCache<AstroFileImpl, Unit>(AstroFramework.ID, file.project, file, Unit) {

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
        if (namedElement !is JSClass)
          consumer(AstroLocalComponent(name, namedElement))
        true
      }, false)
    }
    // Self reference
    consumer(AstroLocalComponent("Astro.self", file, WebSymbol.Priority.NORMAL))
    cacheDependencies.add(file)
  }

}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.lang.AstroFileImpl
import javax.swing.Icon

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
        val resolved = element as? JSPsiNamedElementBase
        val name = resolved?.name
        if (name?.getOrNull(0)?.isUpperCase() != true) return@processDeclarationsInScope true
        if (resolved !is JSClass)
          consumer(AstroLocalComponent(name, resolved))
        true
      }, false)
    }
    cacheDependencies.add(file)
  }

  private class AstroLocalComponent(override val name: String,
                                    override val source: PsiElement) : PsiSourcedWebSymbol {

    override val priority: WebSymbol.Priority
      get() = WebSymbol.Priority.HIGH

    override val origin: WebSymbolOrigin
      get() = AstroLocalSymbolOrigin

    override val namespace: SymbolNamespace
      get() = NAMESPACE_HTML

    override val kind: SymbolKind
      get() = KIND_HTML_ELEMENTS

    override fun createPointer(): Pointer<out WebSymbol> {
      val name = name
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { AstroLocalComponent(name, it) }
      }
    }

  }

  private object AstroLocalSymbolOrigin : WebSymbolOrigin {

    override val defaultIcon: Icon
      get() = AstroIcons.Astro

    override val framework: FrameworkId
      get() = AstroFramework.ID
  }

}
// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf
import org.jetbrains.vuejs.model.VueScopeElement

interface VueScopeElementSymbol : VueElementSymbol, VueScopeElement, PolySymbolScope {

  override val source: PsiElement?

  override fun getModificationCount(): Long = -1

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbol>

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) { symbol, _ ->
      library = getLibraryNameForDocumentationOf(symbol.source)
    }

}
// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf
import org.jetbrains.vuejs.codeInsight.typeOf
import org.jetbrains.vuejs.model.VueElement

interface VueElementSymbol : VueSymbol, VueElement {

  override val searchTarget: PolySymbolSearchTarget?
    get() = PolySymbolSearchTarget.create(this)

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) { symbol, _ ->
      library = getLibraryNameForDocumentationOf(symbol.psiContext)
    }

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", typeOf(this), name))
      .icon(icon)
      .presentation()

  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

}
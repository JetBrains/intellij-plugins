// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols

import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.js.decorateWithSymbolType
import com.intellij.psi.PsiElement
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl

class AstroCodeCompletionItemCustomizer : PolySymbolCodeCompletionItemCustomizer {
  override fun customize(
    item: PolySymbolCodeCompletionItem,
    framework: FrameworkId?,
    kind: PolySymbolKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem =
    item.symbol
      ?.takeIf {
        framework == AstroFramework.ID &&
        location.containingFile is AstroFileImpl &&
        it.kind == ASTRO_COMPONENT_PROPS
      }?.let { item.decorateWithSymbolType(location, it) }
    ?: item
}
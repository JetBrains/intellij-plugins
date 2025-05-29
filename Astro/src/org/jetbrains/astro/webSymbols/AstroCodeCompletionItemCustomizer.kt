// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols

import com.intellij.javascript.polySymbols.decorateWithSymbolType
import com.intellij.psi.PsiElement
import com.intellij.polySymbols.FrameworkId
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl

class AstroCodeCompletionItemCustomizer : PolySymbolCodeCompletionItemCustomizer {
  override fun customize(item: PolySymbolCodeCompletionItem,
                         framework: FrameworkId?,
                         qualifiedKind: PolySymbolQualifiedKind,
                         location: PsiElement) =
    item.symbol
      ?.takeIf {
        framework == AstroFramework.ID &&
        location.containingFile is AstroFileImpl &&
        it.kind == ASTRO_COMPONENT_PROPS.kind
      }?.let { item.decorateWithSymbolType(location, it) }
    ?: item
}
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols

import com.intellij.javascript.webSymbols.decorateWithSymbolType
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.FrameworkId
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItemCustomizer
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl

class AstroCodeCompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {
  override fun customize(item: WebSymbolCodeCompletionItem,
                         framework: FrameworkId?,
                         qualifiedKind: WebSymbolQualifiedKind,
                         location: PsiElement) =
    item.symbol
      ?.takeIf {
        framework == AstroFramework.ID &&
        location.containingFile is AstroFileImpl &&
        it.kind == ASTRO_COMPONENT_PROPS.kind
      }?.let { item.decorateWithSymbolType(location, it) }
    ?: item
}
// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement

interface VueTemplateSymbolScopesProvider {

  /**
   * If Vue expression is injected the @{code hostElement} is not null.
   */
  fun getScopes(
    element: PsiElement,
    hostElement: PsiElement?,
  ): List<PolySymbolScope>

  companion object {
    internal val EP_NAME = ExtensionPointName.create<VueTemplateSymbolScopesProvider>("com.intellij.vuejs.templateSymbolScopesProvider")
  }
}
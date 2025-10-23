// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptServiceCompletionContributor
import com.intellij.openapi.util.registry.Registry

class AstroServiceCompletionContributor : TypeScriptServiceCompletionContributor() {
  override val serviceItemsLimit: Int get() = Registry.get("astro.language.server.completion.serviceItemsLimit").asInteger()

  override fun fillCompletionVariants(parameters: CompletionParameters, resultSet: CompletionResultSet) {
    super.fillCompletionVariants(parameters, resultSet)

    if (resultSet.prefixMatcher.prefix.trim().length < 2) {
      // If we type '<' inside a template part,
      // the Astro language server returns completions for HTML tags (without components) with isIncomplete = false.
      // Restart completion after typing to include components as well.
      resultSet.restartCompletionOnAnyPrefixChange()
    }
  }
}
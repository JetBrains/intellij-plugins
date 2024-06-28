// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.completion

import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptServiceCompletionContributor
import com.intellij.openapi.util.registry.Registry

class AstroServiceCompletionContributor : TypeScriptServiceCompletionContributor() {
  override val serviceItemsLimit: Int get() = Registry.get("astro.language.server.completion.serviceItemsLimit").asInteger()
}
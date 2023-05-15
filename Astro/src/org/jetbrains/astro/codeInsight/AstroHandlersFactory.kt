// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.psi.PsiElement
import org.jetbrains.astro.codeInsight.imports.AstroAddImportExecutor

class AstroHandlersFactory : TypeScriptHandlersFactory() {

  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(object : JSImportExecutorFactory {
      override fun createExecutor(place: PsiElement): JSAddImportExecutor =
        AstroAddImportExecutor(place)
    })
  }
}
// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.psi.PsiElement
import org.angular2.codeInsight.imports.Angular2AddImportExecutor

class Angular2HandlersFactory : TypeScriptHandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(object : JSImportExecutorFactory {
      override fun createExecutor(place: PsiElement): JSAddImportExecutor =
        Angular2AddImportExecutor(place)
    })
  }

}
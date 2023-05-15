// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.ES6HandlersFactory
import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.imports.VueAddImportExecutor

class VueJSHandlersFactory : ES6HandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(VueImportExecutorFactory())
  }
}

class VueTSHandlersFactory : TypeScriptHandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(VueImportExecutorFactory())
  }
}

class VueImportExecutorFactory : JSImportExecutorFactory {
  override fun createExecutor(place: PsiElement): JSAddImportExecutor {
    return VueAddImportExecutor(place)
  }
}
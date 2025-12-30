// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.lang.typescript.TypeScriptHandlersFactory
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.angular2.codeInsight.imports.Angular2AddImportExecutor

private val NG_EP_NAME = ExtensionPointName.create<Angular2JSHandlersFactory>("org.angular2.jsHandlersFactory")

abstract class Angular2JSHandlersFactory {

  abstract fun accept(place: PsiElement): Boolean

  open fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> =
    emptyList()

  open fun createImportPlaceInfo(place: PsiElement, results: Array<out ResolveResult>): JSImportPlaceInfo =
    JSImportPlaceInfo(place, results)
}

class Angular2HandlersFactory : TypeScriptHandlersFactory() {

  override fun createImportPlaceInfo(place: PsiElement, results: Array<out ResolveResult>): JSImportPlaceInfo =
    NG_EP_NAME.extensionList.firstNotNullOf { it.takeIf { it.accept(place)}?.createImportPlaceInfo(place, results) }

  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> =
    NG_EP_NAME.extensionList.firstNotNullOf { it.takeIf { it.accept(place)}?.createImportFactories(place) }

}

class DefaultAngular2HandlersFactory : Angular2JSHandlersFactory() {
  override fun accept(place: PsiElement): Boolean =
    true

  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return listOf(object : JSImportExecutorFactory {
      override fun createExecutor(place: PsiElement): JSAddImportExecutor =
        Angular2AddImportExecutor(place)
    })
  }
}
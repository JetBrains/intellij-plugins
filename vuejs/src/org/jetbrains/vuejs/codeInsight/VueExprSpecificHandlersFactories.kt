// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.controlflow.ControlFlow
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.isEmbeddedBlock
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.vuejs.codeInsight.controlflow.VueControlFlowBuilder
import org.jetbrains.vuejs.codeInsight.refs.VueExprReferenceExpressionResolver
import org.jetbrains.vuejs.lang.html.VueFileType

class VueJSSpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> =
    VueExprReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)

  override fun getControlFlow(scope: JSControlFlowScope): ControlFlow {
    return VueControlFlowBuilder().buildControlFlow(scope)
  }

  override fun getExportScope(element: PsiElement) = getExportScopeImpl(element) { super.getExportScope(element) }
}

class VueTSSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> =
    VueExprReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits) // it implements TypeScriptReferenceExpressionResolver

  override fun getControlFlow(scope: JSControlFlowScope): ControlFlow {
    return VueControlFlowBuilder().buildControlFlow(scope)
  }

  override fun getExportScope(element: PsiElement) = getExportScopeImpl(element) { super.getExportScope(element) }
}

private fun getExportScopeImpl(element: PsiElement, superCall: (element: PsiElement) -> JSElement?): JSElement? {
  if (element is PsiFile || isEmbeddedBlock(element))
    return null
  val file = InjectedLanguageManager.getInstance(element.project).getTopLevelFile(element)
  if (file.fileType is VueFileType) {
    return JSFileReferencesUtil.getModuleForPsiElement(emptyArray(), file) as? JSElement
  }
  return superCall(element)
}
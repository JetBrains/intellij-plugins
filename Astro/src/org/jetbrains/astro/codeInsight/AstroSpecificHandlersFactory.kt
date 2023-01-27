// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.astro.codeInsight.refs.AstroReferenceExpressionResolver
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript

class AstroSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {

  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return AstroReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun createAccessibilityProcessingHandler(place: PsiElement?, skipNsResolving: Boolean): AccessibilityProcessingHandler {
    return AstroAccessibilityProcessingHandler(place)
  }

  override fun getExportScope(element: PsiElement): JSElement? {
    return super.getExportScope(element).let { if (it is AstroFrontmatterScript) it.context as? JSElement else it }
  }

}
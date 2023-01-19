// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.astro.codeInsight.refs.AstroReferenceExpressionResolver

class AstroSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {

  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return AstroReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun createAccessibilityProcessingHandler(place: PsiElement?, skipNsResolving: Boolean): AccessibilityProcessingHandler {
    return AstroAccessibilityProcessingHandler(place)
  }

}
// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.controlflow.ControlFlow
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.vuejs.codeInsight.controlflow.VueControlFlowBuilder
import org.jetbrains.vuejs.codeInsight.refs.VueJSReferenceExpressionResolver

class VueJSSpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> =
    VueJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)

  override fun getControlFlow(scope: JSControlFlowScope): ControlFlow {
    return VueControlFlowBuilder().buildControlFlow(scope)
  }
}


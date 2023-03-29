// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.resolve.processors.JSResolveProcessor
import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.contextOfType
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl

object VueExprStubBasedScopeHandler : JSStubBasedScopeHandler() {

  override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean =
    when (context) {
      is VueJSEmbeddedExpressionContent -> {
        processScriptModule(true, includeParentScopes, context, processor)
      }
      is VueScriptSetupEmbeddedContentImpl -> {
        super.processDeclarationsInScope(context, processor, false)
        && processScriptSetupTypeParameterList(processor, context)
        && (!includeParentScopes || processScriptModule(false, true, context, processor))
      }
      else -> {
        super.processDeclarationsInScope(context, processor, includeParentScopes)
        && (!includeParentScopes ||
            (context.contextOfType(VueJSEmbeddedExpressionContent::class, VueScriptSetupEmbeddedContentImpl::class)
               ?.let { processDeclarationsInScope(it, processor, true) }
             ?: true)
           )
      }
    }

  private fun processScriptModule(setup: Boolean,
                                  includeParentScopes: Boolean,
                                  context: PsiElement,
                                  processor: PsiScopeProcessor) =

    findModule(context, setup)
      ?.let { super.processDeclarationsInScope(it, processor, includeParentScopes) }
    ?: true

  private fun processScriptSetupTypeParameterList(processor: PsiScopeProcessor, context: PsiElement): Boolean {
    val name = if (processor is JSResolveProcessor) processor.name else null
    return VueScriptSetupEmbeddedContentImpl.findScriptSetupTypeParameterList(context)
             ?.typeParameters
             ?.all {
               if (name == null || it.name == name)
                 processor.execute(it, ResolveState.initial())
               else
                 true
             }
           ?: true
  }

}
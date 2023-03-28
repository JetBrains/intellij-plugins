// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFile

object VueExprStubBasedScopeHandler : JSStubBasedScopeHandler() {

  override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean {
    val initialScope = getScope(context)
    return if (initialScope == null)
      processDeclarationsInTemplateScope(context, processor)
    else
      super.processDeclarationsInScope(context, processor, includeParentScopes)
      && (!includeParentScopes || processDeclarationsInTemplateScope(context, processor))
  }

  private fun processDeclarationsInTemplateScope(context: PsiElement,
                                                 processor: PsiScopeProcessor): Boolean {
    // TODO process template scopes
    // Try script setup
    val vueFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context) as? VueFile
                  ?: return true
    findModule(vueFile, true)
      ?.let { if (!super.processDeclarationsInScope(it, processor, false)) return false }

    return true
  }

}
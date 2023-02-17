// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import org.angular2.entities.Angular2ComponentLocator

object Angular2StubBasedScopeHandler : JSStubBasedPsiTreeUtil.JSStubBasedScopeHandler() {

  override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean {
    val initialScope = getScope(context)
    return if (initialScope == null)
      processDeclarationsComponentClassScope(context, processor)
    else
      super.processDeclarationsInScope(context, processor, includeParentScopes)
      && (!includeParentScopes || processDeclarationsComponentClassScope(context, processor))
  }

  private fun processDeclarationsComponentClassScope(context: PsiElement,
                                                     processor: PsiScopeProcessor): Boolean {
    // TODO process template scopes
    val componentClass = Angular2ComponentLocator.findComponentClass(context) ?: return true
    return JSStubBasedPsiTreeUtil.processDeclarationsInScope(componentClass, processor, false)
  }

}
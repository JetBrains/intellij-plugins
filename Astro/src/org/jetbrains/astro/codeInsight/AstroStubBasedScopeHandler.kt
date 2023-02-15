// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.parentOfType
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript

object AstroStubBasedScopeHandler : JSStubBasedPsiTreeUtil.JSStubBasedScopeHandler() {

  override fun processDeclarationsInScope(context: PsiElement, processor: PsiScopeProcessor, includeParentScopes: Boolean): Boolean =
    if (context.parentOfType<AstroFrontmatterScript>(true) != null)
      super.processDeclarationsInScope(context, processor, includeParentScopes)
    else
      super.processDeclarationsInScope(context, processor, includeParentScopes)
      && (!includeParentScopes
          || processDeclarationsInFrontmatterScope(context, processor))

  private fun processDeclarationsInFrontmatterScope(context: PsiElement,
                                                    processor: PsiScopeProcessor): Boolean =
    context.frontmatterScript()
      ?.let { super.processDeclarationsInScope(it, processor, false) } != false

}
// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper
import java.util.function.Consumer

class VueScriptSetupScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> = findModule(element, true)?.let {
    listOf(VueScriptSetupScope(it))
  } ?: emptyList()

  private class VueScriptSetupScope constructor(private val module: JSEmbeddedContent) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      val unwrapRef = VueCompositionInfoHelper.getUnwrapRefType(module)
      JSStubBasedPsiTreeUtil.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        val elementToConsume = VueCompositionInfoHelper.getUnwrappedRefElement(resolved, unwrapRef)
                               ?: resolved ?: element
        consumer.accept(PsiElementResolveResult(elementToConsume, true)).let { true }
      }, false)
    }
  }
}
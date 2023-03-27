// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper
import java.util.function.Consumer

class VueScriptSetupScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return findModule(element, true)?.let { scriptSetupModule ->
      listOfNotNull(VueScriptSetupScope(scriptSetupModule),
                    findModule(element, false)?.let { VueRegularScriptScope(it) }
      )
    } ?: emptyList()
  }

  private class VueScriptSetupScope(private val module: JSExecutionScope) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      JSStubBasedPsiTreeUtil.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        val elementToConsume = VueCompositionInfoHelper.getUnwrappedRefElement(resolved, module)
                               ?: resolved ?: element
        consumer.accept(PsiElementResolveResult(elementToConsume, true)).let { true }
      }, false)
    }
  }

  private class VueRegularScriptScope(private val module: JSExecutionScope) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      JSStubBasedPsiTreeUtil.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        val elementToConsume = resolved ?: element
        consumer.accept(PsiElementResolveResult(elementToConsume, true)).let { true }
      }, false)
    }
  }
}
// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.codeInsight.VueExprStubBasedScopeHandler
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper
import java.util.function.Consumer

class VueScriptSetupScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return findModule(element, true)?.let { scriptSetupModule ->
      listOf(VueScriptSetupScope(scriptSetupModule))
    } ?: emptyList()
  }

  private class VueScriptSetupScope(private val module: JSExecutionScope) : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      VueExprStubBasedScopeHandler.processDeclarationsInScope(module, { element, _ ->
        val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
        // let's patch back in JavaScript plugin resolve conventions
        val importSpecifier = if (element != resolved && element is ES6ImportSpecifier) {
          element
        }
        else null
        val elementToConsume = VueCompositionInfoHelper.getUnwrappedRefElement(resolved, module)
                               ?: resolved ?: element
        consumer.accept(JSResolveResult(elementToConsume, importSpecifier, null))
        true
      }, true)
    }
  }
}
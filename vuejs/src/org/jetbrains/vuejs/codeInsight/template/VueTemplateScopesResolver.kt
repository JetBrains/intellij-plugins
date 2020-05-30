// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

object VueTemplateScopesResolver {

  fun resolve(element: PsiElement, processor: Processor<in ResolveResult>) {
    // TODO merge with Angular code
    val original = CompletionUtil.getOriginalOrSelf(element)
    if (!checkLanguage(original)) {
      return
    }
    val expressionIsInjected = original.containingFile.language.`is`(VueJSLanguage.INSTANCE)
    val hostElement: PsiElement?
    if (expressionIsInjected) {
      //we are working within injection
      hostElement = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
      if (hostElement == null) {
        return
      }
    }
    else {
      hostElement = null
    }

    StreamEx.of(VueTemplateScopesProvider.EP_NAME.extensionList)
      .flatCollection { provider -> provider.getScopes(element, hostElement) }
      .findFirst { s -> !s.processAllScopesInHierarchy(processor) }
  }

  private fun checkLanguage(element: PsiElement): Boolean {
    return (element.language.`is`(VueJSLanguage.INSTANCE)
            || element.language.`is`(VueLanguage.INSTANCE)
            || element.parent != null
            && (element.parent.language.`is`(VueJSLanguage.INSTANCE)
                || element.parent.language.`is`(VueLanguage.INSTANCE)))
  }

}

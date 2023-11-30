// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptWithFileTypeUnionInnerProvider
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.vuejs.lang.html.VueFile

class VueElementResolveScopeProvider : JSElementResolveScopeProvider {
  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    val project = element.project
    val psiFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(element)
    if (psiFile !is VueFile) return null
    if (DialectDetector.isTypeScript(element)) {
      return TypeScriptWithFileTypeUnionInnerProvider.getProvider()
        .getResolveScope(psiFile.viewProvider.virtualFile, project)
    }
    return null
  }
}

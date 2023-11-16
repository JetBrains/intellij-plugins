// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptWithFileTypeUnionInnerProvider
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.astro.lang.AstroFileType

class AstroElementResolveScopeProvider : JSElementResolveScopeProvider {
  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    val file = element.containingFile
    val virtualFile = file?.virtualFile

    if (!element.isValid
        || file?.fileType != AstroFileType.INSTANCE
        || virtualFile == null
        || !DialectDetector.isTypeScript(element))
      return null

    return TypeScriptWithFileTypeUnionInnerProvider.getResolveScope(virtualFile, element.project)
  }
}

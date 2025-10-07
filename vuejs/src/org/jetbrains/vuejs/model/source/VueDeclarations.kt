// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
import com.intellij.lang.javascript.psi.JSPsiReferenceElement
import com.intellij.psi.PsiElement

internal object VueDeclarations {
  fun findDeclaration(
    source: PsiElement,
  ): PsiElement? {
    // copied form `JSGotoDeclarationHandler.getGotoDeclarationTargetsImpl`
    val declarations = when (source) {
      is ES6ImportedBinding -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(source)
      is JSPsiReferenceElement -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(source)
      else -> return null
    }

    return declarations?.singleOrNull()
  }
}

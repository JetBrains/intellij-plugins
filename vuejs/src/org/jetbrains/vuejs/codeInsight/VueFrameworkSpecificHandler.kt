// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.context.hasPinia

class VueFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun useMoreAccurateEvaluation(context: PsiElement): Boolean =
    hasPinia(context)
}
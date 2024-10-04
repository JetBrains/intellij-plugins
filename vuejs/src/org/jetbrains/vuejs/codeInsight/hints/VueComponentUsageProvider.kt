// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.hints

import com.intellij.lang.javascript.hints.JSComponentUsageProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.vuejs.libraries.nuxt.codeInsight.buildNuxtGlobalComponentsScope

class VueComponentUsageProvider : JSComponentUsageProvider {
  override fun contributeSearchScope(file: PsiFile): GlobalSearchScope? = buildNuxtGlobalComponentsScope(file)
}
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.hints

import com.intellij.lang.javascript.hints.JSComponentUsageProvider
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.vuejs.context.hasNuxt
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.libraries.nuxt.NUXT_COMPONENTS_DEFS
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

class VueComponentUsageProvider : JSComponentUsageProvider {
  override fun contributeSearchScope(file: PsiFile): GlobalSearchScope? = buildNuxtGlobalComponentsScope(file)

  private fun buildNuxtGlobalComponentsScope(element: PsiElement): GlobalSearchScope? {
    if (element is VueFile && hasNuxt(element)) {
      val file = element.virtualFile ?: return null

      return JSProjectUtil.processDirectoriesUpToContentRootAndFindFirst(element.project, file) { dir ->
        dir.findChild(NUXT_OUTPUT_FOLDER)?.takeIf { it.isValid && it.isDirectory }?.findChild(NUXT_COMPONENTS_DEFS)
      }?.let { GlobalSearchScope.fileScope(element.project, it) }
    }

    return null
  }
}

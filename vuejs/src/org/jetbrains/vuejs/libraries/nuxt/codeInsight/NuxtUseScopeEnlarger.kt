// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import org.jetbrains.vuejs.context.hasNuxt
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.libraries.nuxt.NUXT_COMPONENTS_DEFS
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

class NuxtUseScopeEnlarger : UseScopeEnlarger() {
  override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
    if (element is VueFile && hasNuxt(element)) {
      val file = element.virtualFile ?: return null

      return JSProjectUtil.processDirectoriesUpToContentRootAndFindFirst(element.project, file) { dir ->
        dir.findChild(NUXT_OUTPUT_FOLDER)?.takeIf { it.isValid && it.isDirectory }?.findChild(NUXT_COMPONENTS_DEFS)
      }?.let { GlobalSearchScope.fileScope(element.project, it) }
    }

    return null
  }
}
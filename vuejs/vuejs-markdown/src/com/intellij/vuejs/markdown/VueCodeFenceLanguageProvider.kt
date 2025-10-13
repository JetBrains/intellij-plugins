// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vuejs.markdown

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.intellij.plugins.markdown.injection.CodeFenceLanguageProvider
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueCodeFenceLanguageProvider : CodeFenceLanguageProvider {
  override fun getLanguageByInfoString(infoString: String): Language? {
    if (infoString.equals(VueLanguage.id, true) || infoString.equals(VueLanguage.displayName, true)) {
      return VueLanguage
    }
    return null
  }

  override fun getInfoStringForLanguage(language: Language, context: PsiElement?): String? {
    if (language == VueLanguage) {
      return "vue"
    }
    return null
  }

  override fun getCompletionVariantsForInfoString(parameters: CompletionParameters): List<LookupElement> = emptyList()
}
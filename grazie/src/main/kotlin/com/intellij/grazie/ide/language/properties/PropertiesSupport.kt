// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.properties

import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiElement
import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.language.LanguageSupport
import com.intellij.grazie.utils.filterNotToSet

class PropertiesSupport : LanguageSupport() {
  companion object {
    private val ignoredCategories = listOf(Typo.Category.CASING)
  }

  override fun isRelevant(element: PsiElement) = element is PropertyValueImpl

  override fun check(element: PsiElement) = GrammarChecker.default.check(element).filterNotToSet { it.info.category in ignoredCategories }
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language.json

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterNotToSet

class JsonSupport : LanguageSupport() {
  companion object {
    private val ignoredCategories = listOf(Typo.Category.CASING)
  }

  override fun isRelevant(element: PsiElement) = element is JsonStringLiteral

  override fun check(element: PsiElement) = GrammarChecker.default.check(element).filterNotToSet { it.info.category in ignoredCategories }
}

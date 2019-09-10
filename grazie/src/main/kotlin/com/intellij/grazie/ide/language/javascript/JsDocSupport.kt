// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.javascript

import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.PsiElement
import com.intellij.grazie.GrazieBundle
import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.language.LanguageSupport

class JsDocSupport : LanguageSupport(GrazieBundle.langConfig("global.literal_string.disabled")) {
  override fun isRelevant(element: PsiElement) = element is JSDocComment

  override fun check(element: PsiElement): Set<Typo> {
    //Ranges of elements that have been parsed by dialect (not general JS).
    //Mostly it is non-text elements, like parameters and document tags.
    //All of them should be considered as `inline elements` and must be ignored.
    val langRanges = element.children.map { it.textRangeInParent }

    return GrammarChecker.default.check(setOf(element), GrammarChecker.TokenRules(
      ignoreByIndex = linkedSetOf({ _, index -> langRanges.any { range -> index in range } }))
    )
  }
}

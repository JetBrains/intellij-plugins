// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.python

import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.ide.language.LanguageSupport
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFormattedStringElement
import com.jetbrains.python.psi.PyPlainStringElement
import com.jetbrains.python.psi.PyStringLiteralExpression

class PDocSupport : LanguageSupport() {
  override fun isRelevant(element: PsiElement) = element is PyStringLiteralExpression && element.isDocString

  override fun check(element: PsiElement) = GrammarChecker.default.check(
    (element as PyStringLiteralExpression).stringElements,
    tokenRules = GrammarChecker.TokenRules(
      ignoreByIndex = linkedSetOf(
        { token, index ->
          when (token) {
            is PyFormattedStringElement -> token.literalPartRanges.all { index !in it }
            is PyPlainStringElement -> false
            else -> false
          }
        })
    )
  )
}

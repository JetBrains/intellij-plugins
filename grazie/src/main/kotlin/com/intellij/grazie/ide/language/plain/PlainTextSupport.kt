// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.language.plain

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.ide.language.LanguageSupport

class PlainTextSupport : LanguageSupport() {
  override fun isRelevant(element: PsiElement) = element is PsiPlainText && element.containingFile.name.endsWith(".txt")

  override fun check(element: PsiElement) = GrammarChecker.default.check(element)
}

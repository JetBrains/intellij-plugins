package com.intellij.lang.javascript.linter.eslint

import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer

class EslintConfigFileSpellcheckingStrategy : SpellcheckingStrategy(), DumbAware {
  override fun getTokenizer(element: PsiElement): Tokenizer<PsiElement> {
    return EMPTY_TOKENIZER
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return EslintUtil.isFlatOrLegacyConfigFile(element)
  }
}

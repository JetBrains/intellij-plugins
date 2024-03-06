// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.spellchecker

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.HeredocContentTokenizer
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.StringLiteralTokenizer
import org.intellij.terraform.hcl.patterns.HCLPatterns.HashesStringLiterals
import org.intellij.terraform.hcl.psi.HCLHeredocContent
import org.intellij.terraform.hcl.psi.HCLHeredocMarker
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLStringLiteral

open class HCLSpellcheckerStrategy : SpellcheckingStrategy() {
  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element == null) return EMPTY_TOKENIZER
    if (element is PsiLanguageInjectionHost && InjectedLanguageManager.getInstance(element.project).getInjectedPsiFiles(element) != null) {
      return EMPTY_TOKENIZER
    }
    if (element is HCLStringLiteral) {
      return if (HashesStringLiterals.accepts(element)) EMPTY_TOKENIZER else StringLiteralTokenizer
    }
    if (element is HCLHeredocContent) {
      return HeredocContentTokenizer
    }
    if (element is HCLHeredocMarker) {
      return TEXT_TOKENIZER
    }
    if (element is HCLIdentifier) {
      return TEXT_TOKENIZER
    }
    if (element is PsiNameIdentifierOwner) {
      return EMPTY_TOKENIZER
    }
    return super.getTokenizer(element)
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return !TerraformPatterns.TerraformFile.accepts(element.containingFile)
  }
}

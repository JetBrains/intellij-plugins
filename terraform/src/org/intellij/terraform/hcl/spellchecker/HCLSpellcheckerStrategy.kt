// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.spellchecker

import com.intellij.codeInspection.SuppressionUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.*
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.config.patterns.TerraformPatterns.DescriptionProperty
import org.intellij.terraform.config.patterns.TerraformPatterns.RootBlockForHCLFiles
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.HeredocContentTokenizer
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.StringLiteralTokenizer
import org.intellij.terraform.hcl.psi.*

open class HCLSpellcheckerStrategy : SpellcheckingStrategy() {
  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element == null) return EMPTY_TOKENIZER

    // Logic for WhiteSpaces and Comments from SpellcheckingStrategy().getTokenizer()
    if (element is PsiWhiteSpace) {
      return EMPTY_TOKENIZER
    }
    if (element is PsiComment) {
      if (SuppressionUtil.isSuppressionComment(element)) {
        return EMPTY_TOKENIZER
      }
      if (element.getTextOffset() == 0 && element.getText().startsWith("#!")) {
        return EMPTY_TOKENIZER
      }
      return myCommentTokenizer
    }

    if (element is PsiLanguageInjectionHost && InjectedLanguageManager.getInstance(element.project).getInjectedPsiFiles(element) != null) {
      return EMPTY_TOKENIZER
    }
    val parent = element.parent
    if (parent is HCLBlock && (element is HCLIdentifier || element is HCLStringLiteral)) {
      if (RootBlockForHCLFiles.accepts(parent)) {
        // may change a last name element in some block types, otherwise name elements are predefined, no sense to report them as typos
        val type = parent.getNameElementUnquoted(0)
        if (element === parent.nameIdentifier && type in TerraformSpellcheckingUtil.RootBlocksWithChangeableName) {
          return if (element is HCLStringLiteral) StringLiteralTokenizer else TEXT_TOKENIZER
        }
      }
    }
    if (element is HCLStringLiteral) {
      return if (DescriptionProperty.accepts(parent)) StringLiteralTokenizer else EMPTY_TOKENIZER
    }
    if (element is HCLHeredocContent) {
      return HeredocContentTokenizer
    }
    if (element is HCLHeredocMarker) {
      return TEXT_TOKENIZER
    }
    if (element is PsiNameIdentifierOwner) {
      return EMPTY_TOKENIZER
    }
    return EMPTY_TOKENIZER
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return element.containingFile is HCLFile
  }
}

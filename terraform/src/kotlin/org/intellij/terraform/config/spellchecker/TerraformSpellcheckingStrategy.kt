// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.spellchecker

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.patterns.TerraformPatterns.DependsOnPattern
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.RootBlocksWithChangeableName
import org.intellij.terraform.config.spellchecker.TerraformSpellcheckingUtil.StringLiteralTokenizer
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.spellchecker.HCLSpellcheckerStrategy

class TerraformSpellcheckingStrategy : HCLSpellcheckerStrategy() {
  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element == null) return EMPTY_TOKENIZER
    val parent = element.parent
    if (parent is HCLBlock && (element is HCLIdentifier || element is HCLStringLiteral)) {
      if (TerraformPatterns.RootBlock.accepts(parent)) {
        // may change last name element in some block types
        // otherwise name elements are predefined, no sense to report them as typos
        val type = parent.getNameElementUnquoted(0)!!
        if (element === parent.nameIdentifier && type in RootBlocksWithChangeableName) {
          return if (element is HCLStringLiteral) StringLiteralTokenizer else TEXT_TOKENIZER
        }
        return EMPTY_TOKENIZER
      }
      if (TerraformPatterns.BlockNameIdentifier.accepts(element)) {
        return EMPTY_TOKENIZER
      }
    }
    if (element is HCLIdentifier && parent is HCLMethodCallExpression && element === parent.method) {
      if (TypeModelProvider.getModel(element).getFunction(element.text) != null) return EMPTY_TOKENIZER
    }

    val inArray = parent?.parent is HCLArray
    val property = element.parentOfType<HCLProperty>()
    if (DependsOnPattern.accepts(property) && inArray) {
      return EMPTY_TOKENIZER
    }
    return super.getTokenizer(element)
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return TerraformPatterns.TerraformFile.accepts(element.containingFile)
  }
}
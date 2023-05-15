// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.spellchecker

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.spellchecker.HCLSpellcheckerStrategy
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.patterns.TerraformPatterns

class TerraformSpellcheckingStrategy : HCLSpellcheckerStrategy() {
  companion object {
    private val RootBlocksWithChangeableName = setOf("output", "variable", "module", "resource", "data")
  }

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
    }
    if (element is HCLIdentifier && parent is HCLMethodCallExpression && element === parent.method) {
      if (ModelHelper.getTypeModel(element.project).getFunction(element.text) != null) return EMPTY_TOKENIZER
    }

    return super.getTokenizer(element)
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return TerraformPatterns.TerraformFile.accepts(element.containingFile)
  }
}
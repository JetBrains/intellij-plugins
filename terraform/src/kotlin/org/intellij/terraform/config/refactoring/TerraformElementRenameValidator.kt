// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.config.patterns.TerraformPatterns


class TerraformElementRenameValidator : RenameInputValidator {
  companion object {
    // From https://www.terraform.io/docs/configuration/variables.html
    private val ProhibitedVariableNames = setOf("source", "version", "providers", "count", "for_each", "lifecycle", "depends_on", "locals")

    private fun isInputValid(name: String): Boolean {
      val length = name.length
      if (length == 0) return false
      if (name[0] != '_' && !Character.isUnicodeIdentifierStart(name[0])) return false
      for (i in 1 until length) {
        val c = name[i]
        if (c != '-' && !Character.isUnicodeIdentifierPart(c)) return false
      }
      return true
    }
  }

  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
        TerraformPatterns.ResourceRootBlock,
        TerraformPatterns.DataSourceRootBlock,
        TerraformPatterns.ModuleRootBlock,
        TerraformPatterns.VariableRootBlock,
        TerraformPatterns.OutputRootBlock
    )
  }

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    return isInputValid(name, element)
  }

  fun isInputValid(name: String, element: PsiElement): Boolean {
    if (!pattern.accepts(element)) return false
    if (TerraformPatterns.VariableRootBlock.accepts(element)) {
      if (HCLPsiUtil.stripQuotes(name).toLowerCase() in ProhibitedVariableNames) return false
    }

    return isInputValid(name)
  }
}

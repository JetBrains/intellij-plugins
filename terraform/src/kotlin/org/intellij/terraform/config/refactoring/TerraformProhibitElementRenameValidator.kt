// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.patterns.TerraformPatterns


class TerraformProhibitElementRenameValidator : RenameInputValidator {
  override fun getPattern(): ElementPattern<out PsiElement> {
    return or(
        TerraformPatterns.TerraformRootBlock,
        TerraformPatterns.ProviderRootBlock,
        TerraformPatterns.LocalsRootBlock
    )
  }

  override fun isInputValid(name: String, element: PsiElement, context: ProcessingContext): Boolean {
    return false
  }
}

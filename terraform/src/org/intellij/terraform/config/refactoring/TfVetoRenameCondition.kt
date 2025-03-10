// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.isTerraformCompatiblePsiFile

internal class TfVetoRenameCondition : Condition<PsiElement> {
  override fun value(element: PsiElement?): Boolean {
    val file = element?.containingFile ?: return false
    return if (isTerraformCompatiblePsiFile(file)) {
      element is HCLIdentifier && !isLocalProperty(element)
    }
    else false
  }

  private fun isLocalProperty(element: HCLIdentifier): Boolean {
    val property = element.parent
    val block = property.parentOfType<HCLBlock>()
    return TfPsiPatterns.LocalsRootBlock.accepts(block)
  }
}
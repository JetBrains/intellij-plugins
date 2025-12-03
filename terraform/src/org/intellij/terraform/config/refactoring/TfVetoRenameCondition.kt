// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.isTfOrTofuPsiFile

internal class TfVetoRenameCondition : Condition<PsiElement> {
  override fun value(element: PsiElement?): Boolean {
    val file = element?.containingFile ?: return false
    if (!isTfOrTofuPsiFile(file)) return false
    return element is HCLIdentifier && isNotHclProperty(element)
  }

  private fun isNotHclProperty(element: HCLIdentifier): Boolean = element.parent !is HCLProperty
}
// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.findUsages

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLPsiUtil

internal class HclReadWriteAccessDetector : ReadWriteAccessDetector() {

  override fun isReadWriteAccessible(element: PsiElement): Boolean =
    element is HCLBlock || isLocalValue(element)

  override fun isDeclarationWriteAccess(element: PsiElement): Boolean = isLocalValue(element)

  override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access =
    getExpressionAccess(reference.element)

  override fun getExpressionAccess(expression: PsiElement): Access = when {
    expression is HCLBlock || expression is HCLProperty || HCLPsiUtil.isPropertyKey(expression) -> Access.Write
    else -> Access.Read
  }

  private fun isLocalValue(element: PsiElement): Boolean {
    val property = when {
      element is HCLProperty -> element
      HCLPsiUtil.isPropertyKey(element) -> element.parent as HCLProperty
      else -> return false
    }

    return TfPsiPatterns.LocalsVariable.accepts(property)
  }
}

// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_COUNT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SELF_IDENTIFIER
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hcl.psi.getDataSource
import org.intellij.terraform.hcl.psi.getHclBlockForSelfContext
import org.intellij.terraform.hcl.psi.getResource
import org.intellij.terraform.hil.psi.impl.getHCLHost

object ILScopeReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    return getReferencesByElement(element)
  }

  private fun getReferencesByElement(element: PsiElement): Array<out PsiReference> {
    if (element !is Identifier) return PsiReference.EMPTY_ARRAY
    element.getHCLHost() ?: return PsiReference.EMPTY_ARRAY

    val parent = element.parent as? SelectExpression<*> ?: return PsiReference.EMPTY_ARRAY
    val from = parent.from as? Identifier ?: return PsiReference.EMPTY_ARRAY

    if (from !== element) return PsiReference.EMPTY_ARRAY

    when (element.name) {
      HCL_SELF_IDENTIFIER -> {
        return arrayOf(HCLElementLazyReference(element, false) { _, _ ->
          val resource = getHclBlockForSelfContext(this.element) ?: return@HCLElementLazyReference emptyList()
          listOf(resource)
        })
      }
      HCL_COUNT_IDENTIFIER -> {
        return arrayOf(HCLElementLazyReference(element, true) { _, _ ->
          listOfNotNull(
            getResource(this.element)?.`object`?.findProperty(HCL_COUNT_IDENTIFIER),
            getDataSource(this.element)?.`object`?.findProperty(HCL_COUNT_IDENTIFIER)
          )
        })
      }
    }
    return PsiReference.EMPTY_ARRAY
  }
}

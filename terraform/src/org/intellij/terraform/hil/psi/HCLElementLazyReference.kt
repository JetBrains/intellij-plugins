// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.psi.TfDocumentPsi
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hil.inspection.PsiFakeAwarePolyVariantReference

abstract class HCLElementLazyReferenceBase<T : PsiElement>(from: T, soft: Boolean) : PsiReferenceBase.Poly<T>(from, soft), PsiFakeAwarePolyVariantReference {
  abstract fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement>

  override fun multiResolve(incompleteCode: Boolean, includeFake: Boolean): Array<out ResolveResult> {
    return PsiElementResolveResult.createResults(resolve(incompleteCode, includeFake))
  }

  override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
    return multiResolve(incompleteCode, false)
  }

  override fun getVariants(): Array<out Any> {
    return EMPTY_ARRAY
  }
}

open class HCLElementLazyReference<T : PsiElement>(from: T, soft: Boolean, val description: (() -> String)? = null, val doResolve: HCLElementLazyReference<T>.(incompleteCode: Boolean, includeFake: Boolean) -> List<PsiElement>) : HCLElementLazyReferenceBase<T>(from, soft) {
  override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement> = doResolve(incompleteCode, includeFake)
  override fun toString(): String {
    if (description != null) {
      return description.invoke() + " " + super.toString()
    }
    return super.toString()
  }

  override fun isReferenceTo(element: PsiElement): Boolean {
    if (element is TfDocumentPsi) {
      element.parentOfType<HCLBlock>()?.let {
        if (super.isReferenceTo(it)) return true
      }
    }
    return super.isReferenceTo(element)
  }
}

interface SpeciallyHandledPsiReference : PsiPolyVariantReference, PsiFakeAwarePolyVariantReference {
  fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>)
}
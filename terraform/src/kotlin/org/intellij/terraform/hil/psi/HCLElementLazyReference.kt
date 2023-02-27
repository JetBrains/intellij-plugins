/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hil.psi

import com.intellij.psi.*
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
}

interface SpeciallyHandledPsiReference : PsiPolyVariantReference, PsiFakeAwarePolyVariantReference {
  fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>)
}
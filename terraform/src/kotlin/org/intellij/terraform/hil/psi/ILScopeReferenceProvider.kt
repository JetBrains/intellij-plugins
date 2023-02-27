/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
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
      "self" -> {
        return arrayOf(HCLElementLazyReference(element, false) { _, _ ->
          val resource = getProvisionerOrConnectionResource(this.element) ?: return@HCLElementLazyReference emptyList()
          listOf(resource)
        })
      }
      "count" -> {
        return arrayOf(HCLElementLazyReference(element, true) { _, _ ->
          listOfNotNull(
              getResource(this.element)?.`object`?.findProperty("count"),
              getDataSource(this.element)?.`object`?.findProperty("count")
          )
        })
      }
    }
    return PsiReference.EMPTY_ARRAY
  }
}

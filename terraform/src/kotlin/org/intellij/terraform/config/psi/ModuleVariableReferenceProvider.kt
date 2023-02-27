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
package org.intellij.terraform.config.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.hil.psi.HCLElementLazyReference

object ModuleVariableReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLIdentifier) return PsiReference.EMPTY_ARRAY
    if (!HCLPsiUtil.isPropertyKey(element)) return PsiReference.EMPTY_ARRAY

    val block = PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, true) ?: return PsiReference.EMPTY_ARRAY

    val type = block.getNameElementUnquoted(0) ?: return PsiReference.EMPTY_ARRAY
    if (type != "module") return PsiReference.EMPTY_ARRAY

    Module.getAsModuleBlock(block) ?: return PsiReference.EMPTY_ARRAY

    return arrayOf(HCLElementLazyReference(element, false) { incomplete, _ ->
      @Suppress("NAME_SHADOWING")
      val element = this.element
      @Suppress("NAME_SHADOWING")
      val block = PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, true) ?: return@HCLElementLazyReference emptyList()
      @Suppress("NAME_SHADOWING")
      val type = block.getNameElementUnquoted(0) ?: return@HCLElementLazyReference emptyList()
      if (type != "module") return@HCLElementLazyReference emptyList()
      val module = Module.getAsModuleBlock(block) ?: return@HCLElementLazyReference emptyList()

      if (incomplete) {
        module.getAllVariables().map { it.declaration }
      } else {
        val value = element.id
        module.findVariables(value.substringBefore('.')).map { it.declaration }
      }
    })
  }
}
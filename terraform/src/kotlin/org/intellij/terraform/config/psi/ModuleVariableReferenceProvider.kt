// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
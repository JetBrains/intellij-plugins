// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hil.psi.HCLElementLazyReferenceBase

object ModuleProvidersReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    // ModuleBlock -> Object -> (Block|Property) -> Object -> Property -> element (either key or value)
    if (element !is HCLElement) return PsiReference.EMPTY_ARRAY
    if (element !is HCLIdentifier && element !is HCLStringLiteral && element !is HCLSelectExpression) return PsiReference.EMPTY_ARRAY
    if (element is HCLIndexSelectExpression) return PsiReference.EMPTY_ARRAY
    val property = element.getParent(HCLProperty::class.java) ?: return PsiReference.EMPTY_ARRAY

    val block = property.getParent(HCLObject::class.java).getParent(HCLObject::class.java).getParent(HCLBlock::class.java)
        ?: return PsiReference.EMPTY_ARRAY

    val type = block.getNameElementUnquoted(0) ?: return PsiReference.EMPTY_ARRAY
    if (type != "module") return PsiReference.EMPTY_ARRAY

    return arrayOf(Reference(element))
  }

  class Reference(element: PsiElement) : HCLElementLazyReferenceBase<PsiElement>(element, false) {
    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<HCLElement> {
      val element = this.element as? HCLElement ?: return emptyList()
      val property = element.getParent(HCLProperty::class.java) ?: return emptyList()
      val block = property.getParent(HCLObject::class.java).getParent(HCLObject::class.java).getParent(HCLBlock::class.java)
          ?: return emptyList()
      val type = block.getNameElementUnquoted(0) ?: return emptyList()
      if (type != "module") return emptyList()
      val value = getValue(element) ?: return emptyList()

      val parent = element.getParent(HCLProperty::class.java)
      val isKey = parent is HCLProperty && HCLPsiUtil.isPartOfPropertyKey(element)
      val module: Module =
          if (isKey) {
            // Key, search referenced module
            Module.getAsModuleBlock(block) ?: return emptyList()
          } else {
            // Value, search current module
            element.getTerraformModule()
          }
      return if (incompleteCode) {
        module.getDefinedProviders().map { it.first }
      } else {
        module.findProviders(value)
      }
    }

    companion object {
      private fun getValue(element: HCLElement): String? {
        if (element is HCLIdentifier && element.parent is HCLSelectExpression) return element.parent.text
        if (element is HCLIdentifier) return element.id
        if (element is HCLStringLiteral) return element.value
        return null
      }
    }
  }
}
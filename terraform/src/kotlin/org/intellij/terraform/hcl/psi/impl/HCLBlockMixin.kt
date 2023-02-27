// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.config.model.getTerraformSearchScope

abstract class HCLBlockMixin(node: ASTNode) : HCLValueWithReferencesMixin(node), HCLBlock {

  abstract override fun getName(): String

  override fun getNameIdentifier(): PsiElement {
    return nameElements.last()!! // Block always have at least one nameElement
  }

  override fun getTextOffset(): Int {
    return nameIdentifier.textOffset
  }

  @Throws(IncorrectOperationException::class)
  override fun setName(name: String): PsiElement {
    // TODO: Should we change only nameIdentifier or other nameElement also?
    ElementChangeUtil.doNameReplacement(this, nameIdentifier, name, HCLElementTypes.IDENTIFIER)
    return this
  }

  override fun getUseScope(): SearchScope {
    return this.getTerraformSearchScope()
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean {
    return this === another || another === nameIdentifier
  }
}

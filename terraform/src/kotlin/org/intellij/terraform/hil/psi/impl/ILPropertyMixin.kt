// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.impl.ElementChangeUtil
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.hil.psi.ILProperty
import org.jetbrains.annotations.NonNls

abstract class ILPropertyMixin(node: ASTNode) : ILExpressionBase(node), ILProperty {

  abstract override fun getName(): String

  @Throws(IncorrectOperationException::class)
  override fun setName(@NonNls name: String): PsiElement {
    ElementChangeUtil.doNameReplacement(this, nameIdentifier, name, HCLElementTypes.IDENTIFIER)
    return this
  }

  override fun getNameIdentifier(): PsiElement {
    return nameElement
  }

  override fun getTextOffset(): Int {
    return nameIdentifier.textOffset
  }

  override fun getUseScope(): SearchScope {
    return this.getTerraformSearchScope()
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean {
    return this === another || another === nameIdentifier
  }
}

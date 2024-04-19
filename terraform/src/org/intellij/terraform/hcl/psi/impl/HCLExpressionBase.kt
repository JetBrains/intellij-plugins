// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLExpression

abstract class HCLExpressionBase(node: ASTNode) : ASTWrapperPsiElement(node), HCLExpression {
  open fun accept(visitor: HCLElementVisitor) {
    visitor.visitExpression(this)
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is HCLElementVisitor) accept(visitor) else super.accept(visitor)
  }

  override fun toString(): String {
    val name = this.javaClass.simpleName
    return StringUtil.trimEnd(name, "Impl")
  }
}

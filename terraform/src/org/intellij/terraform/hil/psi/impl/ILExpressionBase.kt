// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.ILGeneratedVisitor

abstract class ILExpressionBase(node: ASTNode) : ASTWrapperPsiElement(node), ILExpression {

  open fun accept(visitor: ILGeneratedVisitor) {
    visitor.visitElement(this)
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is ILGeneratedVisitor) accept(visitor) else super.accept(visitor)
  }

  override fun toString(): String {
    val name = this.javaClass.simpleName
    val trimmed = name.removeSuffix("Impl")
    if (trimmed.startsWith("ILBinary")) return "ILBinaryExpression"
    if ("ILLiteralExpression" == trimmed || "ILParameterListExpression" == trimmed) return trimmed.removeSuffix("Expression")
    return trimmed
  }

}

internal fun BaseExpression.getHCLHost(): HCLElement? {
  if (this is HCLElement) {
    return this
  }
  val host = InjectedLanguageManager.getInstance(this.project).getInjectionHost(this)
  return host as? HCLElement
}

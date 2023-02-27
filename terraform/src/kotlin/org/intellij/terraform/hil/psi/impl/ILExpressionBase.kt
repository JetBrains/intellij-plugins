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
package org.intellij.terraform.hil.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.text.StringUtil
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
    val trimmed = StringUtil.trimEnd(name, "Impl")
    if (trimmed.startsWith("ILBinary")) return "ILBinaryExpression"
    if ("ILLiteralExpression" == trimmed || "ILParameterListExpression" == trimmed) return StringUtil.trimEnd(trimmed, "Expression")
    return trimmed
  }

}

fun BaseExpression.getHCLHost(): HCLElement? {
  if (this is HCLElement) {
    return this
  }
  val host = InjectedLanguageManager.getInstance(this.project).getInjectionHost(this)
  return if (host is HCLElement) host else null
}

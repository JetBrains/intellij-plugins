// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight.hint

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.MethodCallExpression
import org.intellij.terraform.hcl.psi.common.ParameterList
import org.intellij.terraform.hcl.psi.common.UnaryExpression
import org.intellij.terraform.config.model.getType

class HCL2TypeProvider : ExpressionTypeProvider<BaseExpression>() {
  override fun getInformationHint(element: BaseExpression): String {
    val type = element.getType()
    val text = type?.toString() ?: "<unknown>"
    return StringUtil.escapeXmlEntities(text)
  }

  override fun getExpressionsAt(elementAt: PsiElement): List<BaseExpression> {
    return SyntaxTraverser.psiApi().parents(elementAt)
        .filterIsInstance(BaseExpression::class.java)
        .filter { isLargestNonTrivialExpression(it) }
        .toList()
  }

  override fun getErrorHint(): String {
    return "No expression found"
  }

  private fun isLargestNonTrivialExpression(e: BaseExpression): Boolean {
    val p = e.parent
    return when {
      p is UnaryExpression<*> -> false
      p is MethodCallExpression<*> && p.callee === e -> false
      p is ParameterList<*> -> false
      p is HCLBlock -> false
      HCLPsiUtil.isPartOfPropertyKey(e) -> false
      else -> true
    }
  }
}
/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
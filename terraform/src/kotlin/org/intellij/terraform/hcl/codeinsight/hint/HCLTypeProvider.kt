// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight.hint

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.config.codeinsight.ModelHelper

class HCLTypeProvider : ExpressionTypeProvider<HCLElement>() {
  override fun getInformationHint(element: HCLElement): String {
    var text: String
    if (element is HCLBlock) {
      text = "block"
      val type = ModelHelper.getBlockType(element)
      if (type != null) {
        text = type.presentableText
      }

    } else if (element is HCLProperty) {
      text = "property"
    } else {
      text = "<unknown>"
    }
    return StringUtil.escapeXmlEntities(text)
  }

  override fun getExpressionsAt(elementAt: PsiElement): List<HCLElement> {
    return SyntaxTraverser.psiApi().parents(elementAt)
        .filterIsInstance(HCLElement::class.java)
        .filter { isSupportedExpression(it) }
        .toList()
  }

  override fun getErrorHint(): String {
    return "No expression found"
  }

  private fun isSupportedExpression(e: HCLElement): Boolean {
    return when {
      e is HCLProperty -> true
      e is HCLBlock -> true
      else -> false
    }
  }
}
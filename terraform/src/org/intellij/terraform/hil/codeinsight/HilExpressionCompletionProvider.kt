// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.BaseExpression

internal abstract class HilExpressionCompletionProvider : CompletionProvider<CompletionParameters>() {
  abstract val expressionPosition: PsiElementPattern.Capture<PsiElement>

  fun registerTo(contributor: CompletionContributor) {
    contributor.extend(CompletionType.BASIC, expressionPosition, this)
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val expression = parameters.position.parent as? BaseExpression ?: return

    val property = PsiTreeUtil.getParentOfType(expression, HCLProperty::class.java, false, HCLBlock::class.java)
    if (property != null && property.nameIdentifier == expression) return

    // Add all Terraform functions by default
    val model = TypeModelProvider.getModel(expression)
    result.addAllElements(model.functions.map { createFunction(it) })

    addAdditionalCompletions(expression, result)
  }

  open fun addAdditionalCompletions(expression: PsiElement, result: CompletionResultSet) {}
}

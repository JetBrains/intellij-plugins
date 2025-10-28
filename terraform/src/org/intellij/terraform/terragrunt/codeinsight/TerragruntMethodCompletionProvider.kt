// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createScopeLookup
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.terragrunt.isTerragruntStack
import org.intellij.terraform.terragrunt.model.TerragruntFunctions

internal object TerragruntMethodCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val expression = parameters.position.parent as? BaseExpression ?: return

    val property = PsiTreeUtil.getParentOfType(expression, HCLProperty::class.java, false, HCLBlock::class.java)
    if (property != null && property.nameIdentifier == expression) return

    addTerragruntFunctions(result)
    addTfFunctions(expression, result)
    addScopeCompletions(expression, result)
  }

  private fun addTerragruntFunctions(result: CompletionResultSet) {
    result.addAllElements(TerragruntFunctions.map { createFunction(it, isTerragrunt = true) })
  }

  private fun addTfFunctions(expression: PsiElement, result: CompletionResultSet) {
    val model = TypeModelProvider.getModel(expression)
    result.addAllElements(model.functions.map { createFunction(it) })
  }

  private fun addScopeCompletions(expression: PsiElement, result: CompletionResultSet) {
    val topLevelFile = InjectedLanguageManager.getInstance(expression.project).getTopLevelFile(expression) ?: return
    val scopes = if (isTerragruntStack(topLevelFile)) {
      TerragruntUnitHelper.StackScope
    }
    else {
      TerragruntUnitHelper.TerragruntScope
    }
    result.addAllElements(scopes.map { createScopeLookup(it) })
  }
}
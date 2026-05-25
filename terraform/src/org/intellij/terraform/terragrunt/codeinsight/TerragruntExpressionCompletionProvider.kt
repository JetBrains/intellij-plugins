// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createScopeLookup
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hil.codeinsight.HilExpressionCompletionProvider
import org.intellij.terraform.hil.patterns.HILPatterns.getExpressionPattern
import org.intellij.terraform.terragrunt.isTerragruntStack
import org.intellij.terraform.terragrunt.model.TerragruntFunctions
import org.intellij.terraform.terragrunt.patterns.TerragruntPsiPatterns.TerragruntFile

internal object TerragruntExpressionCompletionProvider : HilExpressionCompletionProvider() {
  override val expressionPosition: PsiElementPattern.Capture<PsiElement> = getExpressionPattern(
    PlatformPatterns.psiElement(HCLElement::class.java)
      .inFile(TerragruntFile)
  )

  override fun addAdditionalCompletions(expression: PsiElement, result: CompletionResultSet) {
    addTerragruntFunctions(result)
    addScopeCompletions(expression, result)
  }

  private fun addTerragruntFunctions(result: CompletionResultSet) {
    result.addAllElements(TerragruntFunctions.map { createFunction(it, isTerragrunt = true) })
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
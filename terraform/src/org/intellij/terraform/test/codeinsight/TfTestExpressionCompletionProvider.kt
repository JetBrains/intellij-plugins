// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.codeinsight

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hil.codeinsight.HilExpressionCompletionProvider
import org.intellij.terraform.hil.patterns.HILPatterns.getExpressionPattern
import org.intellij.terraform.test.patterns.TfTestPsiPatterns.TfTestFile

internal object TfTestExpressionCompletionProvider : HilExpressionCompletionProvider() {
  override val expressionPosition: PsiElementPattern.Capture<PsiElement> = getExpressionPattern(
    PlatformPatterns.psiElement(HCLElement::class.java)
      .inFile(TfTestFile)
  )

  override fun addAdditionalCompletions(expression: PsiElement, result: CompletionResultSet) {
    val model = TypeModelProvider.getModel(expression)
    result.addAllElements(model.providerDefinedFunctions.map { createFunction(it) })
  }
}

// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.terragrunt.model.TerragruntFunctions

internal object TerragruntMethodCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val parent = parameters.position.parent as? BaseExpression ?: return

    val property = PsiTreeUtil.getParentOfType(parent, HCLProperty::class.java, false, HCLBlock::class.java)
    if (property != null && property.nameIdentifier == parent) return
    result.addAllElements(TerragruntFunctions.map { createFunction(it, isTerragrunt = true) })

    val model = TypeModelProvider.getModel(parent)
    result.addAllElements(model.functions.map { createFunction(it) })
  }
}
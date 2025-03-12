// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import org.intellij.terraform.template.model.TftplVariable
import org.intellij.terraform.template.model.collectAvailableVariables

internal class HilTemplateCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, hilVariablePattern, HilTemplateVariableCompletionProvider())
  }
}

private class HilTemplateVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    collectAvailableVariables(parameters.position).forEach { variable ->
      ProgressManager.checkCanceled()
      result.addElement(createVariableLookup(variable))
    }
  }

  private fun createVariableLookup(variable: TftplVariable): LookupElement {
    return createVariableLookupSkeleton(variable)
      .let { lookup -> PrioritizedLookupElement.withPriority(lookup, HIGH_COMPLETION_PRIORITY) }
  }
}
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hil.HILElementType
import org.intellij.terraform.hil.HILTokenType
import org.intellij.terraform.template.model.TftplVariable
import org.intellij.terraform.template.model.collectAvailableVariables

internal class TftplDataLanguageCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, unparsedSegmentPattern, TftplVariableCompletionProvider())
  }
}

private val unparsedSegmentPattern: PsiElementPattern.Capture<PsiElement> = PlatformPatterns.psiElement()
  .with(
    object : PatternCondition<PsiElement>("isHclTemplateDataLanguageButNotInjection") {
      override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        return element.elementType !is HILElementType
               && element.elementType !is HILTokenType
               && getTemplateFileViewProvider(element) != null
               && (element.elementType != HCLElementTypes.DOUBLE_QUOTED_STRING
                   || !InjectedLanguageManager.getInstance(element.project).isInjectedFragment(element.containingFile))
      }
    }
  )

private class TftplVariableCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val effectivePosition = translateToTemplateLanguageElement(parameters.position) ?: return
    collectAvailableVariables(effectivePosition).forEach { variable ->
      ProgressManager.checkCanceled()
      // avoid prefix-matching because the preceding text is treated like an outer language element
      result.withPrefixMatcher("").addElement(createVariableLookup(variable))
    }
  }

  private fun createVariableLookup(variable: TftplVariable): LookupElement {
    return createVariableLookupSkeleton(variable)
      .withInsertHandler { context, item ->
        context.run {
          document.replaceString(startOffset, tailOffset, escapeLookup(item.lookupString))
        }
      }.let { lookup -> PrioritizedLookupElement.withPriority(lookup, HIGH_COMPLETION_PRIORITY) }
  }

  private fun escapeLookup(lookupText: String): String {
    return "\${$lookupText}"
  }
}

package com.intellij.webassembly.lang

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.webassembly.lang.psi.WebAssemblyTypes


class WebAssemblyKeywordCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
           psiElement(WebAssemblyTypes.RPAR),
           WebAssemblyKeywordCompletionProvider())
  }

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    super.fillCompletionVariants(parameters, result)
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    context.dummyIdentifier = ""
  }
}

package org.jetbrains.webstorm.lang

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import org.jetbrains.webstorm.lang.psi.WebAssemblyTypes


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

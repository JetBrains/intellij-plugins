package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.dts.completion.contributer.withDtsPrefixMatcher
import com.intellij.dts.completion.getDtsContainer
import com.intellij.util.ProcessingContext

class DtsRootNodeProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val container = context.getDtsContainer()
        val resultSet = result.withDtsPrefixMatcher(parameters)

        if (!container.isDtsRootContainer || container.dtsAffiliation.isNode()) return

        val lookup = LookupElementBuilder.create("/")
            .withTailText(" {}")
            .withTypeText(DtsBundle.message("documentation.node_type"))
            .withIcon(DtsIcons.Node)
            .withInsertHandler(DtsInsertHandler.ROOT_NODE)

        resultSet.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.ROOT_NODE))
    }
}
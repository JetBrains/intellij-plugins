package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.completion.contributer.withDtsPrefixMatcher
import com.intellij.dts.completion.getDtsContainer
import com.intellij.dts.lang.DtsAffiliation
import com.intellij.util.ProcessingContext

private val rootDirectives = setOf(
  "/dts-v1/",
  "/plugin/",
  "/include/",
  "/memreserve/",
  "/delete-node/",
  "/omit-if-no-ref/",
)

private val nodeDirectives = setOf(
  "/delete-property/",
  "/delete-node/",
  "/include/",
  "/omit-if-no-ref/",
)

class DtsCompilerDirectiveProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val container = context.getDtsContainer()
    val resultSet = result.withDtsPrefixMatcher(parameters)

    val directives = when (container.dtsAffiliation) {
      DtsAffiliation.ROOT -> rootDirectives
      DtsAffiliation.NODE -> nodeDirectives
      DtsAffiliation.UNKNOWN -> rootDirectives.union(nodeDirectives)
    }

    for (directive in directives) {
      val lookup = LookupElementBuilder.create(directive)
      resultSet.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.COMPILER_DIRECTIVE))
    }
  }
}
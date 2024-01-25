package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.dts.completion.getDtsContainer
import com.intellij.dts.documentation.DtsNodeBindingDocumentationTarget
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.isDtsRootNode
import com.intellij.dts.lang.symbols.DtsDocumentationSymbol
import com.intellij.dts.zephyr.binding.DtsZephyrBundledBindings
import com.intellij.util.ProcessingContext

object DtsNodeNameProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val container = context.getDtsContainer()
    val resultSet = result.withDtsPrefixMatcher(parameters)

    val node = container.parent as? DtsNode ?: return

    if (!node.isDtsRootNode()) return

    // no removal of present nodes in suggestions, because some nodes can be
    // suffixed with @... which makes them different

    val provider = DtsZephyrBundledBindings.getInstance()

    for (name in DtsZephyrBundledBindings.NODE_BINDINGS) {
      val binding = provider.getBinding(name) ?: continue

      val symbol = DtsDocumentationSymbol.from(DtsNodeBindingDocumentationTarget(
        node.project,
        name,
        binding,
      ))

      val lookup = LookupElementBuilder.create(symbol, name)
        .withTypeText(DtsBundle.message("documentation.node.lookup_type"))
        .withIcon(DtsIcons.Node)
        .withInsertHandler(DtsInsertHandler.SUB_NODE)

      resultSet.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.SUB_NODE))
    }
  }
}
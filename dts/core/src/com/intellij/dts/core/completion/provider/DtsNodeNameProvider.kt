package com.intellij.dts.core.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.core.DtsBundle
import com.intellij.dts.core.completion.getDtsContainer
import com.intellij.dts.core.documentation.DtsNodeBindingDocumentationTarget
import com.intellij.dts.core.icons.DtsCoreIcons
import com.intellij.dts.core.lang.psi.DtsNode
import com.intellij.dts.core.lang.psi.isDtsRootNode
import com.intellij.dts.core.lang.symbols.DtsDocumentationSymbol
import com.intellij.dts.core.zephyr.binding.DtsZephyrBundledBindings
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
        .withIcon(DtsCoreIcons.Node)
        .withInsertHandler(DtsInsertHandler.SUB_NODE)

      resultSet.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.SUB_NODE))
    }
  }
}
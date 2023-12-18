package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsIcons
import com.intellij.dts.completion.getDtsContainer
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.symbols.DtsPropertySymbol
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.util.ProcessingContext

class DtsPropertyNameProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val container = context.getDtsContainer()
    val resultSet = result.withDtsPrefixMatcher(parameters)

    val node = container.parent as? DtsNode ?: return

    val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

    val presentProperties = node.dtsProperties.map { it.dtsName }
    val newProperties = binding.properties.values.filter { !presentProperties.contains(it.name) }

    for (property in newProperties) {
      val lookup = LookupElementBuilder.create(DtsPropertySymbol(property).createPointer(), property.name)
        .withTypeText(property.type.typeName)
        .withIcon(DtsIcons.Property)
        .withInsertHandler(DtsInsertHandler.PROPERTY)

      resultSet.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.PROPERTY))
    }
  }
}
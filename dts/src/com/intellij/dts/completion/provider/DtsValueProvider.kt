package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.completion.getDtsProperty
import com.intellij.dts.lang.DtsPropertyValue
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.util.ProcessingContext

private inline fun <reified T : DtsPropertyValue> getValues(context: ProcessingContext): List<T>? {
  val property = context.getDtsProperty()

  val binding = DtsZephyrBindingProvider.bindingFor(property) ?: return null
  val values = binding.enum?.filterIsInstance<T>() ?: return null

  if (values.isEmpty()) return null

  return values
}

object DtsStringValueProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val values = getValues<DtsPropertyValue.String>(context) ?: return

    for (element in values) {
      result.addElement(LookupElementBuilder.create(element.value))
    }
  }
}

object DtsIntValueProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val values = getValues<DtsPropertyValue.Int>(context) ?: return

    val resultSet = result.withDtsIntPrefixMatcher(parameters)

    for (element in values) {
      resultSet.addElement(LookupElementBuilder.create(element.value))
    }
  }
}

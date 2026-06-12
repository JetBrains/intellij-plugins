package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.microservices.jvm.config.yaml.ConfigYamlUtils
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLMapping

internal class OpenRewriteYamlKeyCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val element = parameters.position
    val originalElement = CompletionUtil.getOriginalElement(element)
    val parentYamlKeyValue = ConfigYamlUtils.getParentKeyValue(element, originalElement) ?: return
    val type = getKeyValueType(parentYamlKeyValue) ?: return

    val keyText = parentYamlKeyValue.keyText
    val descriptor =
      OpenRewriteRecipeService.getInstance(element.project).findDescriptor(keyText, parameters.originalFile, type) ?: return

    val existingOptions = (parentYamlKeyValue.value as? YAMLMapping)?.keyValues?.map { it.keyText }?.toSet() ?: emptySet()

    val optionElements = descriptor.options
      .filter { !existingOptions.contains(it.name) }
      .map { getOptionLookupElement(it) }
    result.addAllElements(optionElements)
    result.stopHere()
  }
}
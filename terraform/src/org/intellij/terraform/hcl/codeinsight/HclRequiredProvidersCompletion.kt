// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.buildLookupForRequiredProvider
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLObject

internal object TfRequiredProviderCompletion : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val element = parameters.originalPosition ?: return
    val superParent = element.parent?.parent

    // Completion of all types of providers
    if (TfPsiPatterns.TfRequiredProvidersBlock.accepts(superParent)) {
      val prefix = result.prefixMatcher.prefix
      val providers = TypeModelProvider.getModel(element).allProviders()
        .filter { it.type.startsWith(prefix) }
        .map { buildLookupForRequiredProvider(it, element) }
        .toList()

      val sorter = CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("tf.providers.weigher") {
        override fun weigh(element: LookupElement): Int {
          val providerType = element.`object` as? ProviderType
          return if (providerType?.tier in ProviderTier.PreferedProviders) 0 else 1
        }
      })
      result.withRelevanceSorter(sorter).addAllElements(providers)
    }
    // Completion properties in the required provider type
    else {
      val parent = element.parentOfType<HCLObject>() ?: return
      if (!TfPsiPatterns.RequiredProvidersProperty.accepts(parent.parent))
        return

      val properties = listOf("source", "version").map { PropertyType(it, Types.String) }.filter { parent.findProperty(it.name) == null }
      result.addAllElements(properties.map { createPropertyOrBlockType(it) })
    }
  }
}

package com.intellij.dts.core.lang.resolve

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.core.documentation.DtsBindingDocumentation
import com.intellij.dts.core.lang.psi.DtsString
import com.intellij.dts.core.lang.symbols.DtsBindingSymbol
import com.intellij.dts.core.lang.symbols.DtsDocumentationSymbol
import com.intellij.dts.core.util.DtsTreeUtil
import com.intellij.dts.core.util.DtsUtil
import com.intellij.dts.core.util.relativeTo
import com.intellij.dts.core.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiCompletableReference
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class DtsBindingReference(private val element: DtsString) : PsiSymbolReference, PsiCompletableReference {
  override fun getElement(): PsiElement = element

  override fun getRangeInElement(): TextRange = element.dtsValueRange.relativeTo(element.textRange)

  override fun resolveReference(): Collection<Symbol> {
    val node = DtsTreeUtil.parentNode(element) ?: return emptyList()

    val provider = DtsZephyrBindingProvider.of(element.project)
    val binding = provider.getBinding(node, listOf(element.dtsParse())) ?: return emptyList()

    return DtsUtil.singleResult { DtsBindingSymbol(binding) }
  }

  override fun getCompletionVariants(): Collection<LookupElement> {
    val project = element.project
    val variants = mutableListOf<LookupElement>()
    val provider = DtsZephyrBindingProvider.of(project)

    for (binding in provider.getAllBindings()) {
      val compatible = binding.compatible ?: continue
      val symbol = DtsDocumentationSymbol.from(DtsBindingDocumentation(project, binding))

      variants.add(LookupElementBuilder.create(symbol, compatible))
    }

    return variants
  }
}
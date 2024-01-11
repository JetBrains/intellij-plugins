package com.intellij.dts.lang.resolve

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.documentation.DtsBindingDocumentation
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.symbols.DtsBindingSymbol
import com.intellij.dts.lang.symbols.DtsDocumentationSymbol
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.util.relativeTo
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
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
    val binding = DtsZephyrBindingProvider.bindingFor(node, fallbackBinding = false) ?: return emptyList()

    return DtsUtil.singleResult { DtsBindingSymbol(binding) }
  }

  override fun getCompletionVariants(): MutableCollection<LookupElement> {
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
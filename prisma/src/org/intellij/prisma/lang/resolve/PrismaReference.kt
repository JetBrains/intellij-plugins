package org.intellij.prisma.lang.resolve

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.resolve.ResolveCache
import org.intellij.prisma.lang.psi.PrismaElementFactory
import org.intellij.prisma.lang.psi.PrismaNamedElement
import org.intellij.prisma.lang.psi.PrismaReferenceElement
import org.intellij.prisma.lang.psi.PrismaTypeOwner
import org.intellij.prisma.lang.types.typeText

abstract class PrismaReference(
  element: PsiElement,
  range: TextRange,
  soft: Boolean = false,
) : PsiPolyVariantReferenceBase<PsiElement>(element, range, soft) {

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    return ResolveCache.getInstance(element.project).resolveWithCaching(this, RESOLVER, false, false)
  }

  private fun resolveInner(): Array<ResolveResult> {
    val processor = createResolveProcessor(element) ?: return ResolveResult.EMPTY_ARRAY
    val state = ResolveState.initial()
    processCandidates(processor, state, element)
    return PsiElementResolveResult.createResults(processor.getResults())
  }

  protected open fun createResolveProcessor(element: PsiElement): PrismaResolveProcessor? {
    val name = (element as? PrismaReferenceElement)?.referenceName ?: return null
    return PrismaResolveProcessor(name, element)
  }

  protected open fun createCompletionProcessor(element: PsiElement): PrismaProcessor {
    return PrismaCompletionProcessor()
  }

  override fun getVariants(): Array<Any> {
    val processor = createCompletionProcessor(element)
    processCandidates(processor, ResolveState.initial(), element)

    val results = processor.getResults()
    if (results.isNotEmpty()) {
      val ignored = collectIgnoredNames()
      return results
        .filter { it.name !in ignored }
        .map { createLookupElement(it) }
        .toTypedArray()
    }

    return emptyArray()
  }

  private fun createLookupElement(element: PrismaNamedElement): LookupElementBuilder {
    var lookup = LookupElementBuilder.createWithIcon(element)
    if (element is PrismaTypeOwner) {
      lookup = lookup.withTypeText(element.type.typeText)
    }
    return lookup
  }

  protected abstract fun processCandidates(
    processor: PrismaProcessor,
    state: ResolveState,
    element: PsiElement,
  )

  protected open fun collectIgnoredNames(): Set<String> = emptySet()

  override fun handleElementRename(newElementName: String): PsiElement {
    val referenceElement = element as? PrismaReferenceElement
    if (referenceElement != null) {
      val nameElement = referenceElement.referenceNameElement
      if (nameElement != null) {
        val identifier = PrismaElementFactory.createIdentifier(referenceElement.project, newElementName)
        return nameElement.replace(identifier)
      }
    }

    return super.handleElementRename(newElementName)
  }

  companion object {
    private val RESOLVER = ResolveCache.PolyVariantResolver<PrismaReference> { ref, _ -> ref.resolveInner() }
  }
}
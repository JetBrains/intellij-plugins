package org.angular2.findUsages

import com.intellij.find.usages.api.PsiUsage
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor.JSNonRenameableReference
import com.intellij.model.search.Searcher
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import com.intellij.util.Query
import com.intellij.webSymbols.search.WebSymbolUsageQueries
import org.angular2.entities.Angular2EntitiesProvider

class Angular2ComponentClassInTemplateUsageSearcher : Searcher<ReferencesSearch.SearchParameters, PsiReference> {

  override fun collectSearchRequests(parameters: ReferencesSearch.SearchParameters): Collection<@JvmWildcard Query<out PsiReference>> {
    val element = parameters.elementToSearch as? TypeScriptClass ?: return emptyList()
    val directive = Angular2EntitiesProvider.getDirective(element) ?: return emptyList()
    val project = parameters.project

    return directive.selector.simpleSelectorsWithPsi.flatMap { it.allSymbols }
      .filter { JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) { it.referencedSymbol == null } }
      .flatMap { WebSymbolUsageQueries.buildWebSymbolUsagesQueries(it, project, parameters.effectiveSearchScope) }
      .map { it.filtering { !it.declaration }.mapping { it.toPsiReference(element) } }
  }

  private fun PsiUsage.toPsiReference(element: TypeScriptClass): PsiReference {
    val source = PsiTreeUtil.findElementOfClassAtRange(file, range.startOffset, range.endOffset, PsiElement::class.java)
    return ClassUsageInTemplateFile(source ?: file, range.shiftLeft(source?.startOffset ?: 0), element)
  }

  class ClassUsageInTemplateFile(source: PsiElement, range: TextRange, private val element: PsiElement)
    : JSNonRenameableReference, PsiReferenceBase<PsiElement>(source, range, true) {
    override fun resolve(): PsiElement? = element
    override fun bindToElement(element: PsiElement): PsiElement? = null
    override fun handleElementRename(newElementName: String): PsiElement? = null
  }

}
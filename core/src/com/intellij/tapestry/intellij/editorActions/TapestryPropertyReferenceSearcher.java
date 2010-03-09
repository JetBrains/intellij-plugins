package com.intellij.tapestry.intellij.editorActions;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;

/**
 * @author Alexey Chmutov
 *         Date: Dec 10, 2009
 *         Time: 6:38:33 PM
 */
public class TapestryPropertyReferenceSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  public boolean execute(final ReferencesSearch.SearchParameters queryParameters, final Processor<PsiReference> consumer) {
    final PsiElement refElement = queryParameters.getElementToSearch();
    if (!(refElement instanceof PsiField)) return true;
    final String name = ((PsiField)refElement).getName();
    if (name == null) return true;

    //SearchScope searchScope = ApplicationManager.getApplication().runReadAction(new Computable<SearchScope>() {
    //  public SearchScope compute() {
    //    return queryParameters.getEffectiveSearchScope();
    //  }
    //});
    SearchScope searchScope = queryParameters.getScope();
    if (searchScope instanceof GlobalSearchScope) {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)searchScope, TmlFileType.INSTANCE);
    }
    else {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(refElement.getProject()), TmlFileType.INSTANCE).intersectWith(searchScope);
    }
    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      public boolean execute(PsiElement element, int offsetInElement) {
        ProgressManager.checkCanceled();
        final PsiReference[] refs = element.getReferences();
        for (PsiReference ref : refs) {
          if (ref.getRangeInElement().contains(offsetInElement) && ref.isReferenceTo(refElement)) {
            return consumer.process(ref);
          }
        }
        return true;
      }
    };
    final PsiSearchHelper helper = PsiManager.getInstance(refElement.getProject()).getSearchHelper();
    final boolean propertyFound =
      helper.processElementsWithWord(processor, searchScope, name, UsageSearchContext.IN_FOREIGN_LANGUAGES, false);
    final boolean getterFound =
      helper.processElementsWithWord(processor, searchScope, "get" + name, UsageSearchContext.IN_FOREIGN_LANGUAGES, false);
    final boolean setterFound =
      helper.processElementsWithWord(processor, searchScope, "set" + name, UsageSearchContext.IN_FOREIGN_LANGUAGES, false);
    return propertyFound || getterFound || setterFound;
  }
}


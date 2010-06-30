package com.intellij.tapestry.intellij.editorActions;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.Processor;

/**
 * @author Alexey Chmutov
 *         Date: Dec 10, 2009
 *         Time: 6:38:33 PM
 */
public class TapestryPropertyReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  public TapestryPropertyReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(ReferencesSearch.SearchParameters queryParameters, Processor<PsiReference> consumer) {
    final PsiElement refElement = queryParameters.getElementToSearch();
    if (!(refElement instanceof PsiField)) return;
    final String name = ((PsiField)refElement).getName();
    if (name == null) return;
    SearchScope searchScope = queryParameters.getScope();
    if (searchScope instanceof GlobalSearchScope) {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)searchScope, TmlFileType.INSTANCE);
    }
    else {
      searchScope =
        GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(refElement.getProject()), TmlFileType.INSTANCE)
          .intersectWith(searchScope);
    }
    queryParameters.getOptimizer().searchWord(name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
    queryParameters.getOptimizer().searchWord("get" + name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
    queryParameters.getOptimizer().searchWord("set" + name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
  }
}


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
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TapestryPropertyReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  public TapestryPropertyReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiElement refElement = queryParameters.getElementToSearch();
    if (!(refElement instanceof PsiField)) return;
    final String name = ((PsiField)refElement).getName();
    SearchScope searchScope = queryParameters.getEffectiveSearchScope();
    if (searchScope instanceof GlobalSearchScope) {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)searchScope, TmlFileType.INSTANCE);
    }
    else {
      searchScope =
        GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(refElement.getProject()), TmlFileType.INSTANCE)
          .intersectWith(searchScope);
    }
    if (searchScope instanceof GlobalSearchScope && ((GlobalSearchScope)searchScope).getProject() == null) return;

    queryParameters.getOptimizer().searchWord(name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
    queryParameters.getOptimizer().searchWord("get" + name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
    queryParameters.getOptimizer().searchWord("set" + name, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, refElement);
  }
}


package com.intellij.tapestry.intellij.editorActions;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.tapestry.intellij.util.TapestryPropertyNamingUtil;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TapestryMethodReferenceSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {

  public TapestryMethodReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull MethodReferencesSearch.SearchParameters parameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiMethod method = parameters.getMethod();
    final String propName = TapestryPropertyNamingUtil.getPropertyNameFromAccessor(method);

    SearchScope searchScope = parameters.getEffectiveSearchScope();
    if (searchScope instanceof GlobalSearchScope) {
      searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)searchScope, TmlFileType.INSTANCE);
    }
    if (!StringUtil.isEmpty(propName)) {
      parameters.getOptimizer().searchWord(propName, searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, method);
    }
    parameters.getOptimizer().searchWord(method.getName(), searchScope, UsageSearchContext.IN_FOREIGN_LANGUAGES, false, method);
  }
}

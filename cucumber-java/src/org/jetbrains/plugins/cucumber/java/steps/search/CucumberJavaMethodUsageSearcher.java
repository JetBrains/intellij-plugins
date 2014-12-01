package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

/**
 * User: Andrey.Vokin
 * Date: 7/27/12
 */
public class CucumberJavaMethodUsageSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {
  public CucumberJavaMethodUsageSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull final MethodReferencesSearch.SearchParameters p, @NotNull final Processor<PsiReference> consumer) {
    final PsiMethod method = p.getMethod();

    final PsiAnnotation stepAnnotation = CucumberJavaUtil.getCucumberStepAnnotation(method);
    final String regexp = stepAnnotation != null ? CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation) : null;
    if (regexp == null) {
      return;
    }

    final String word = CucumberUtil.getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmpty(word)) {
      return;
    }

    SearchScope scope = p.getEffectiveSearchScope();
    if (scope instanceof GlobalSearchScope) {
      GlobalSearchScope restrictedScope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)scope, GherkinFileType.INSTANCE);
      ReferencesSearch.search(new ReferencesSearch.SearchParameters(method, restrictedScope, false, p.getOptimizer())).forEach(consumer);
    }
  }
}

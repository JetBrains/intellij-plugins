package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * User: Andrey.Vokin
 * Date: 7/25/12
 */
public class CucumberJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    if (!(element instanceof PsiMethod)){
      return true;
    }


    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}

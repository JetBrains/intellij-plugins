package org.intellij.plugins.postcss.usages;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;

public class PostCssUsageSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters searchParameters,
                           @NotNull Processor<? super PsiReference> processor) {
    PsiElement targetElement = searchParameters.getElementToSearch();
    if (targetElement instanceof PostCssSimpleVariableDeclaration) {
      String name = ReadAction.compute(() -> ((PostCssSimpleVariableDeclaration)targetElement).getName());
      searchParameters.getOptimizer().searchWord(name, searchParameters.getEffectiveSearchScope(), true, targetElement);
    }
  }
}

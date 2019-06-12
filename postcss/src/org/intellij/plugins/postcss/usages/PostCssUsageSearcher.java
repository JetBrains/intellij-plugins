package org.intellij.plugins.postcss.usages;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariable;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.intellij.plugins.postcss.references.PostCssSimpleVariableReference;
import org.jetbrains.annotations.NotNull;

public class PostCssUsageSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters searchParameters,
                           @NotNull Processor<? super PsiReference> processor) {
    PsiElement targetElement = searchParameters.getElementToSearch();
    if (targetElement instanceof PostCssSimpleVariableDeclaration) {
      String nameWithDollar = "$" + ReadAction.compute(() -> ((PostCssSimpleVariableDeclaration)targetElement).getName());
      searchParameters.getOptimizer().searchWord(nameWithDollar, searchParameters.getEffectiveSearchScope(), UsageSearchContext.IN_CODE,
                                                 true, targetElement, new RequestResultProcessor() {
          @Override
          public boolean processTextOccurrence(@NotNull PsiElement element,
                                               int offsetInElement,
                                               @NotNull Processor<? super PsiReference> consumer) {
            if (!targetElement.isValid()) return false;
            ProgressManager.checkCanceled();

            if (element instanceof PostCssSimpleVariable) {
              for (PsiReference reference : element.getReferences()) {
                if (reference instanceof PostCssSimpleVariableReference && reference.isReferenceTo(targetElement)) {
                  if (!consumer.process(reference)) {
                    return false;
                  }
                }
              }
            }
            return true;
          }
        });
    }
  }
}

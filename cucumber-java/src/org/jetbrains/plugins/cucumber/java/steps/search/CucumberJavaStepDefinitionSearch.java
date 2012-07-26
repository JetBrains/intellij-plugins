package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil;

/**
 * User: Andrey.Vokin
 * Date: 7/25/12
 */
public class CucumberJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters, @NotNull final Processor<PsiReference> consumer) {
    final PsiElement myElement = queryParameters.getElementToSearch();
    if (!(myElement instanceof PsiMethod)){
      return true;
    }
    if (!CucumberJavaUtil.isStepDefinition(myElement)) {
      return false;
    }

    final PsiMethod method = (PsiMethod)myElement;
    final PsiAnnotation stepAnnotation = CucumberJavaUtil.getCucumberAnnotation(method);
    final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);

    final String word = org.jetbrains.plugins.cucumber.CucumberUtil.getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmpty(word)) {
      return true;
    }

    final SearchScope searchScope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(new Computable<SearchScope>() {
      public SearchScope compute() {
        return queryParameters.getEffectiveSearchScope();
      }
    });
    // As far as default CacheBasedRefSearcher doesn't look for references in string we have to write out own to handle this correctly
    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      @Override
      public boolean execute(PsiElement element, int offsetInElement) {
        for (PsiReference ref : element.getReferences()) {
          if (ref != null && ref.isReferenceTo(myElement)) {
            if (!consumer.process(ref)){
              return false;
            }
          }
        }
        return true;
      }
    };

    short context = UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE;
    return PsiSearchHelper.SERVICE.getInstance(myElement.getProject()).
      processElementsWithWord(processor, searchScope, word, context, true);
  }
}

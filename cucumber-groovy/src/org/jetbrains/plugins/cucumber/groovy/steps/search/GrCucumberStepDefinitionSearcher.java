
package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.steps.search.CucumberStepSearchUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Max Medvedev
 */
public class GrCucumberStepDefinitionSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters, @NotNull final Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    if (!GrCucumberUtil.isStepDefinition(element)) return true;

    @Nullable
    final String regexp = ApplicationManager.getApplication().runReadAction(new NullableComputable<String>() {
      @Nullable
      @Override
      public String compute() {
        return GrCucumberUtil.getStepDefinitionPatternText((GrMethodCall)element);
      }
    });
    if (StringUtil.isEmptyOrSpaces(regexp)) return true;

    final String word = CucumberUtil.getTheBiggestWordToSearchByIndex(regexp);
    if (StringUtil.isEmptyOrSpaces(word)) return true;

    final SearchScope searchScope = CucumberStepSearchUtil.restrictScopeToGherkinFiles(new Computable<SearchScope>() {
      public SearchScope compute() {
        return queryParameters.getEffectiveSearchScope();
      }
    });


    // As far as default CacheBasedRefSearcher doesn't look for references in string we have to write out own to handle this correctly
    final TextOccurenceProcessor processor = new TextOccurenceProcessor() {
      @Override
      public boolean execute(final PsiElement occurrence, int offsetInElement) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
          @NotNull
          @Override
          public Boolean compute() {
            if (!processRefs(occurrence, element, consumer)) return false;

            PsiElement parent = occurrence.getParent();
            if (parent != null) {
              if (!processRefs(parent, element, consumer)) return false;
            }
            return true;
          }
        });
      }
    };

    short context = UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_CODE;
    PsiSearchHelper instance = PsiSearchHelper.SERVICE.getInstance(element.getProject());
    return instance.processElementsWithWord(processor, searchScope, word, context, true);
  }

  private static boolean processRefs(PsiElement refOwner, PsiElement toSearchFor, Processor<PsiReference> consumer) {
    for (PsiReference ref : refOwner.getReferences()) {
      if (ref != null && ref.isReferenceTo(toSearchFor)) {
        if (!consumer.process(ref)) return false;
      }
    }
    return true;
  }
}

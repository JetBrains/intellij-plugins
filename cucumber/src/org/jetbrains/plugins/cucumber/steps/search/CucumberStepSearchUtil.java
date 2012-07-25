package org.jetbrains.plugins.cucumber.steps.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

/**
 * User: Andrey.Vokin
 * Date: 7/25/12
 */
public class CucumberStepSearchUtil {
  public static SearchScope restrictScopeToGherkinFiles(final Computable<SearchScope> originalScopeComputation) {
    return ApplicationManager.getApplication().runReadAction(new Computable<SearchScope>() {
      public SearchScope compute() {
        final SearchScope originalScope = originalScopeComputation.compute();
        if (originalScope instanceof GlobalSearchScope) {
          return GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)originalScope,
                                                                 GherkinFileType.INSTANCE);
        }
        return originalScope;
      }
    });
  }
}

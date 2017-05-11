package org.jetbrains.plugins.cucumber.steps.search;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class CucumberStepSearchUtil {
  public static SearchScope restrictScopeToGherkinFiles(final Computable<SearchScope> originalScopeComputation) {
    return ReadAction.compute(() -> {
      final SearchScope originalScope = originalScopeComputation.compute();
      if (originalScope instanceof GlobalSearchScope) {
        return GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)originalScope,
                                                               GherkinFileType.INSTANCE);
      }
      return originalScope;
    });
  }
}

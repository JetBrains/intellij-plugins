package org.jetbrains.plugins.cucumber.steps.search;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class CucumberStepSearchUtil {
  @NotNull
  public static SearchScope restrictScopeToGherkinFiles(@NotNull final SearchScope originalScope) {
    if (originalScope instanceof GlobalSearchScope) {
      return GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)originalScope, GherkinFileType.INSTANCE);
    }

    return originalScope;
  }
}

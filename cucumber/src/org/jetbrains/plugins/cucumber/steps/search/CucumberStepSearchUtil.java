// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps.search;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public final class CucumberStepSearchUtil {
  public static @NotNull SearchScope restrictScopeToGherkinFiles(final @NotNull SearchScope originalScope) {
    if (originalScope instanceof GlobalSearchScope) {
      return GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)originalScope, GherkinFileType.INSTANCE);
    }

    return originalScope;
  }
}

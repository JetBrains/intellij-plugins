// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.resolve.JSCompletionPlaceFilter;
import com.intellij.lang.javascript.psi.resolve.JSDefaultPlaceFilters;
import com.intellij.lang.javascript.psi.resolve.filters.JSReferenceListFilterProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionScriptReferenceListFilterProvider extends JSReferenceListFilterProvider {

  @Override
  public @Nullable JSCompletionPlaceFilter forPlace(@NotNull PsiElement place) {
    if (!DialectDetector.isActionScript(place)) return null;
    return super.forPlace(place);
  }

  @Override
  protected @NotNull JSCompletionPlaceFilter getImplementsFilter() {
    return JSDefaultPlaceFilters.INTERFACE_FILTER;
  }
}

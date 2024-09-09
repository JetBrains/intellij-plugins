// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.navigation;

import com.intellij.lang.javascript.navigation.DumbAwareChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Dennis.Ushakov
 */
public final class AngularGotoSymbolContributor extends DumbAwareChooseByNameContributor {
  @Override
  public void doProcessNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    Project project = Objects.requireNonNull(scope.getProject());
    ContainerUtil.process(AngularIndexUtil.getAllKeys(AngularSymbolIndex.KEY, project), processor);
  }

  @Override
  public void doProcessElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
    AngularIndexUtil.multiResolve(parameters.getProject(), parameters.getSearchScope(),
                                  AngularSymbolIndex.KEY, name, processor);
  }
}

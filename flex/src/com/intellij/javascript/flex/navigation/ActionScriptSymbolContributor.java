// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.navigation;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.navigation.DumbAwareChooseByNameContributor;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.BackendJSResolveUtil;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class ActionScriptSymbolContributor extends DumbAwareChooseByNameContributor implements ChooseByNameContributorEx, DumbAware {
  @Override
  protected void doProcessNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    StubIndex.getInstance().processAllKeys(JSIndexKeys.JS_NAME_INDEX_KEY, processor, scope, filter);
  }

  @Override
  public void doProcessElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
    Iterator<JSQualifiedNamedElement> elements = BackendJSResolveUtil
      .findElementsByName(name, parameters.getProject(), parameters.getSearchScope())
      .stream()
      .filter(o -> DialectDetector.isActionScript(o))
      .filter(o -> !o.getContainingFile().getVirtualFile().getName().endsWith(".swf"))
      .iterator();
    ContainerUtil.process(elements, processor);
  }
}

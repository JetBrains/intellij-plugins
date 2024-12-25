// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartCallHierarchyProvider implements HierarchyProvider {
  @Override
  public @Nullable PsiElement getTarget(@NotNull DataContext dataContext) {
    return DartHierarchyUtil.getResolvedElementAtCursor(dataContext);
  }

  @Override
  public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
    return new DartCallHierarchyBrowser(target.getProject(), target);
  }

  @Override
  public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartCallHierarchyBrowser)hierarchyBrowser).changeView(CallHierarchyBrowserBase.getCallerType());
  }
}

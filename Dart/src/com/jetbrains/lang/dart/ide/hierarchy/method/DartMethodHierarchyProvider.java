// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.method;

import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.ide.hierarchy.MethodHierarchyBrowserBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartGetterDeclaration;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import com.jetbrains.lang.dart.psi.DartSetterDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartMethodHierarchyProvider implements HierarchyProvider {
  @Override
  public @Nullable PsiElement getTarget(@NotNull DataContext dataContext) {
    PsiElement element = DartHierarchyUtil.getResolvedElementAtCursor(dataContext);
    if ((element instanceof DartMethodDeclaration ||
         element instanceof DartGetterDeclaration ||
         element instanceof DartSetterDeclaration) &&
        PsiTreeUtil.getParentOfType(element, DartClass.class) != null) {
      return element;
    }
    return null;
  }

  @Override
  public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
    return new DartMethodHierarchyBrowser(target.getProject(), target);
  }

  @Override
  public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartMethodHierarchyBrowser)hierarchyBrowser).changeView(MethodHierarchyBrowserBase.getMethodType());
  }
}

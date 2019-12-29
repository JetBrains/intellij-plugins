package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCallHierarchyProvider implements HierarchyProvider {
  @Nullable
  @Override
  public PsiElement getTarget(@NotNull DataContext dataContext) {
    return DartHierarchyUtil.getResolvedElementAtCursor(dataContext);
  }

  @NotNull
  @Override
  public HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
    return new DartCallHierarchyBrowser(target.getProject(), target);
  }

  @Override
  public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartCallHierarchyBrowser)hierarchyBrowser).changeView(CallHierarchyBrowserBase.getCALLER_TYPE());
  }
}

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

public class DartMethodHierarchyProvider implements HierarchyProvider {
  @Nullable
  @Override
  public PsiElement getTarget(@NotNull DataContext dataContext) {
    PsiElement element = DartHierarchyUtil.getResolvedElementAtCursor(dataContext);
    if ((element instanceof DartMethodDeclaration ||
         element instanceof DartGetterDeclaration ||
         element instanceof DartSetterDeclaration) &&
        PsiTreeUtil.getParentOfType(element, DartClass.class) != null) {
      return element;
    }
    return null;
  }

  @NotNull
  @Override
  public HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
    return new DartMethodHierarchyBrowser(target.getProject(), target);
  }

  @Override
  public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartMethodHierarchyBrowser)hierarchyBrowser).changeView(MethodHierarchyBrowserBase.METHOD_TYPE);
  }
}

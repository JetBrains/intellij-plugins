package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartNewExpression;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.DartTokenTypes.*;

public class DartCallHierarchyProvider implements HierarchyProvider {
  @Nullable
  @Override
  public PsiElement getTarget(@NotNull DataContext dataContext) {
    return DartHierarchyUtil.getResolvedElementAtCursor(dataContext);
  }

  @NotNull
  @Override
  public HierarchyBrowser createHierarchyBrowser(PsiElement target) {
    return new DartCallHierarchyBrowser(target.getProject(), target);
  }

  @Override
  public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartCallHierarchyBrowser)hierarchyBrowser).changeView(CallHierarchyBrowserBase.CALLER_TYPE);
  }

  private static DartReference getRightmostReference(PsiElement element) {
    PsiElement last = PsiTreeUtil.getDeepestLast(element);
    return PsiTreeUtil.getParentOfType(last, DartReference.class);
  }
}

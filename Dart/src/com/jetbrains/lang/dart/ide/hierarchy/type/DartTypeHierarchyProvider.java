// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartTypeHierarchyProvider implements HierarchyProvider {
  @Override
  public @Nullable DartClass getTarget(final @NotNull DataContext dataContext) {
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (project == null || editor == null) return null;

    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final PsiElement psiElement = file == null ? null : file.findElementAt(editor.getCaretModel().getOffset());
    final DartReference dartReference = PsiTreeUtil.getParentOfType(psiElement, DartReference.class);
    if (dartReference != null) {
      return dartReference.resolveDartClass().getDartClass();
    }
    return PsiTreeUtil.getParentOfType(psiElement, DartClass.class);
  }

  @Override
  public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
    return new DartTypeHierarchyBrowser(target.getProject(), (DartClass)target);
  }

  @Override
  public void browserActivated(final @NotNull HierarchyBrowser hierarchyBrowser) {
    ((DartTypeHierarchyBrowser)hierarchyBrowser).changeView(TypeHierarchyBrowserBase.getTypeHierarchyType());
  }
}

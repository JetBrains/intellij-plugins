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

public class DartTypeHierarchyProvider implements HierarchyProvider {
  private Project project;

  @Nullable
  @Override
  public PsiElement getTarget(@NotNull final DataContext dataContext) {
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor == null) return null;
    project = CommonDataKeys.PROJECT.getData(dataContext);
    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());
    final DartReference dartReference = PsiTreeUtil.getParentOfType(at, DartReference.class);
    if (dartReference != null) {
      return dartReference.resolveDartClass().getDartClass();
    }
    return PsiTreeUtil.getParentOfType(at, DartClass.class);
  }

  @NotNull
  @Override
  public HierarchyBrowser createHierarchyBrowser(final PsiElement target) {
    return new DartTypeHierarchyBrowser(target.getProject(), (DartClass)target);
  }

  @Override
  public void browserActivated(@NotNull final HierarchyBrowser hierarchyBrowser) {
    final DartTypeHierarchyBrowser browser = (DartTypeHierarchyBrowser)hierarchyBrowser;
    final String typeName =
      browser.isInterface() ? TypeHierarchyBrowserBase.SUBTYPES_HIERARCHY_TYPE : TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE;
    browser.changeView(typeName);
  }
}

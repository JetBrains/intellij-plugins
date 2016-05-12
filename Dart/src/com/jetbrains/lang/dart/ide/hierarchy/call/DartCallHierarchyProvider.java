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
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
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
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (project == null || editor == null) return null;

    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final PsiElement psiElement = file == null ? null : file.findElementAt(editor.getCaretModel().getOffset());
    DartReference dartReference = PsiTreeUtil.getParentOfType(psiElement, DartReference.class);
    if (dartReference != null) {
      if (dartReference.getTokenType() == NEW_EXPRESSION) {
        DartComponent cons = DartResolveUtil.findConstructorDeclaration((DartNewExpression)dartReference);
        if (cons != null && cons.getTokenType() == METHOD_DECLARATION) {
          return cons;
        }
        else {
          return null; // Class with no constructor.
        }
      }
      if (dartReference.getTokenType() == CALL_EXPRESSION) {
        dartReference = getRightmostReference(dartReference.getFirstChild());
      }
      DartComponent comp = DartResolveUtil.findReferenceAndComponentTarget(dartReference);
      if (comp != null) {
        return comp;
      }
    }
    else {
      DartComponent comp = PsiTreeUtil.getParentOfType(psiElement, DartComponent.class);
      if (comp != null) {
        return comp;
      }
    }
    return null;
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

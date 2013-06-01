package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCreateFix extends FixAndIntentionAction {
  @Override
  public boolean startInWriteAction() {
    return true;
  }

  protected static void navigate(Project project, Editor editor, int offset, @Nullable VirtualFile vfile) {
    if (ApplicationManager.getApplication().isHeadlessEnvironment()) {
      editor.getCaretModel().moveToOffset(offset);
    }
    else if (vfile != null) {
      new OpenFileDescriptor(project, vfile, offset).navigate(true); // properly contributes to editing history
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    }
  }

  @Nullable
  protected PsiElement findAnchor(PsiElement element) {
    PsiElement scopeBody = getScopeBody(element);
    PsiElement result = element;
    while (result.getParent() != null && result.getParent() != scopeBody) {
      result = result.getParent();
    }
    return result;
  }

  @Nullable
  protected PsiElement getScopeBody(PsiElement element) {
    return null;
  }
}

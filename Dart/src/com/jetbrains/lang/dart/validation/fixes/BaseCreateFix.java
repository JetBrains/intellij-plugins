package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCreateFix extends FixAndIntentionAction {
  @Override
  public boolean startInWriteAction() {
    return true;
  }

  protected static boolean isInDartSdkOrDartPackagesFolder(final @NotNull PsiFile psiFile) {
    final VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
    return vFile != null && DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile.getProject(), vFile);
  }

  @Nullable
  protected static Editor navigate(Project project, int offset, @Nullable VirtualFile vfile) {
    if (vfile == null) {
      return null;
    }
    new OpenFileDescriptor(project, vfile, offset).navigate(true); // properly contributes to editing history
    FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(vfile);
    if (fileEditor instanceof TextEditor) {
      final Editor editor = ((TextEditor)fileEditor).getEditor();
      editor.getCaretModel().moveToOffset(offset);
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
      return editor;
    }
    return null;
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

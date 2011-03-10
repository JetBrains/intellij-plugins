package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractInterfaceHandler;
import com.intellij.lang.javascript.refactoring.memberPullUp.JSPullUpHandler;
import com.intellij.lang.javascript.refactoring.memberPushDown.JSPushDownHandler;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;

public class MxmlRefactoringSupportProvider extends RefactoringSupportProvider {

  @Override
  public boolean isAvailable(@NotNull PsiElement context) {
    PsiFile containingFile = context.getContainingFile();
    return containingFile != null && JavaScriptSupportLoader.isFlexMxmFile(containingFile);
  }

  @Override
  public RefactoringActionHandler getExtractInterfaceHandler() {
    return new JSExtractInterfaceHandler();
  }

  @Override
  public RefactoringActionHandler getPullUpHandler() {
    return new JSPullUpHandler();
  }

  @Override
  public RefactoringActionHandler getPushDownHandler() {
    return new JSPushDownHandler();
  }
}

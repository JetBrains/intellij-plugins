package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.refactoring.JavascriptRefactoringSupportProvider;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractInterfaceHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperClassHandler;
import com.intellij.lang.javascript.refactoring.introduceConstant.JSIntroduceConstantHandler;
import com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldHandler;
import com.intellij.lang.javascript.refactoring.memberPullUp.JSPullUpHandler;
import com.intellij.lang.javascript.refactoring.memberPushDown.JSPushDownHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.changeSignature.ChangeSignatureHandler;
import org.jetbrains.annotations.NotNull;

public class ActionScriptRefactoringSupportProvider extends JavascriptRefactoringSupportProvider {

  @Override
  public boolean isAvailable(@NotNull PsiElement context) {
    PsiFile containingFile = context.getContainingFile();
    return containingFile != null && containingFile.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  @Override
  public RefactoringActionHandler getExtractInterfaceHandler() {
    return new JSExtractInterfaceHandler();
  }

  @Override
  public RefactoringActionHandler getExtractSuperClassHandler() {
    return new JSExtractSuperClassHandler();
  }

  @Override
  public RefactoringActionHandler getPullUpHandler() {
    return new JSPullUpHandler();
  }

  @Override
  public RefactoringActionHandler getPushDownHandler() {
    return new JSPushDownHandler();
  }

  @Override
  public RefactoringActionHandler getIntroduceConstantHandler() {
    return new JSIntroduceConstantHandler();
  }

  @Override
  public RefactoringActionHandler getIntroduceFieldHandler() {
    return new JSIntroduceFieldHandler();
  }
}

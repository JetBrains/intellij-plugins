// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.javascript.flex.refactoring.extractSuper.FlexExtractInterfaceDialog;
import com.intellij.javascript.flex.refactoring.extractSuper.FlexExtractSuperClassDialog;
import com.intellij.javascript.flex.refactoring.introduceConstant.FlexIntroduceConstantHandler;
import com.intellij.lang.javascript.refactoring.JavascriptRefactoringSupportProvider;
import com.intellij.lang.javascript.refactoring.extractMethod.ActionScriptExtractFunctionHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractInterfaceHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperClassHandler;
import com.intellij.lang.javascript.refactoring.introduceField.JSIntroduceFieldHandler;
import com.intellij.lang.javascript.refactoring.introduceVariable.ActionScriptIntroduceVariableHandler;
import com.intellij.lang.javascript.refactoring.memberPullUp.JSPullUpHandler;
import com.intellij.lang.javascript.refactoring.memberPushDown.JSPushDownHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActionScriptRefactoringSupportProvider extends JavascriptRefactoringSupportProvider {

  @Override
  public boolean isAvailable(@NotNull PsiElement context) {
    PsiFile containingFile = context.getContainingFile();
    return containingFile != null && containingFile.getLanguage().is(FlexSupportLoader.ECMA_SCRIPT_L4);
  }

  @Override
  public RefactoringActionHandler getExtractInterfaceHandler() {
    return new JSExtractInterfaceHandler(((aClass, element) -> new FlexExtractInterfaceDialog(aClass, element)));
  }

  @Override
  public RefactoringActionHandler getExtractSuperClassHandler() {
    return new JSExtractSuperClassHandler((aClass, element) -> new FlexExtractSuperClassDialog(aClass, element));
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
  public @Nullable RefactoringActionHandler getIntroduceVariableHandler() {
    return new ActionScriptIntroduceVariableHandler();
  }

  @Override
  public @Nullable RefactoringActionHandler getExtractMethodHandler() {
    return new ActionScriptExtractFunctionHandler();
  }

  @Override
  public RefactoringActionHandler getIntroduceConstantHandler() {
    return new FlexIntroduceConstantHandler();
  }

  @Override
  public RefactoringActionHandler getIntroduceFieldHandler() {
    return new JSIntroduceFieldHandler();
  }
}

package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.refactoring.RefactoringActionHandler;
import com.jetbrains.lang.dart.ide.refactoring.extract.DartServerExtractMethodHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceFinalVariableHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceVariableHandler;
import com.jetbrains.lang.dart.psi.DartNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartRefactoringSupportProvider extends RefactoringSupportProvider {
  @Override
  public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
    return element instanceof DartNamedElement &&
           element.getUseScope() instanceof LocalSearchScope;
  }

  @Override
  public RefactoringActionHandler getIntroduceVariableHandler() {
    return new DartIntroduceVariableHandler();
  }

  @Nullable
  @Override
  public RefactoringActionHandler getIntroduceConstantHandler() {
    return new DartIntroduceFinalVariableHandler();
  }

  @Nullable
  @Override
  public RefactoringActionHandler getExtractMethodHandler() {
    //return new DartExtractMethodHandler();
    return new DartServerExtractMethodHandler();
  }
}

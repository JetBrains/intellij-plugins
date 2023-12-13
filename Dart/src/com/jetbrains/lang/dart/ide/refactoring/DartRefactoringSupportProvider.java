// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.refactoring.RefactoringActionHandler;
import com.jetbrains.lang.dart.ide.refactoring.extract.DartServerExtractMethodHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartServerExtractLocalVariableHandler;
import com.jetbrains.lang.dart.psi.DartNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartRefactoringSupportProvider extends RefactoringSupportProvider {
  @Override
  public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
    return element instanceof DartNamedElement &&
           element.getUseScope() instanceof LocalSearchScope;
  }

  @Override
  public RefactoringActionHandler getIntroduceVariableHandler() {
    return new DartServerExtractLocalVariableHandler();
  }

  @Nullable
  @Override
  public RefactoringActionHandler getExtractMethodHandler() {
    return new DartServerExtractMethodHandler();
  }
}

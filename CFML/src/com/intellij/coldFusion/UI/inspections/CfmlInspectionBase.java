// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public abstract class CfmlInspectionBase extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(final @NotNull PsiElement element) {
        registerProblems(element, holder);
      }
    };
  }

  protected abstract void registerProblems(final PsiElement element, final ProblemsHolder holder);

  @Override
  public @Nls @NotNull String getGroupDisplayName() {
    return CfmlBundle.message("cfml.inspections.group");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}

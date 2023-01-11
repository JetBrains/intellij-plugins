// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
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
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull final PsiElement element) {
        registerProblems(element, holder);
      }
    };
  }

  protected abstract void registerProblems(final PsiElement element, final ProblemsHolder holder);

  @Override
  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return CfmlBundle.message("cfml.inspections.group");
  }

  @Override
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return super.getDefaultLevel();
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}

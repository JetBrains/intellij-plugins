// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class FlexUnitSuiteInspectionBase extends LocalInspectionTool {
  @Override
  public @Nls @NotNull String getGroupDisplayName() {
    return FlexBundle.message("flexunit.inspections.group");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new FlexUnitInspectionVisitor() {
      @Override
      public void visitJSClass(@NotNull JSClass aClass) {
        FlexUnitSupport support = getFlexUnitSupport(aClass);
        if (support == null || !support.isSuite(aClass)) return;
        visitSuite(aClass, holder, support);
      }
    };
  }

  protected abstract void visitSuite(JSClass aClass, final @NotNull ProblemsHolder holder, FlexUnitSupport support);

}
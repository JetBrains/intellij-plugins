package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class FlexUnitClassInspectionBase extends LocalInspectionTool {
  @Override
  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return FlexBundle.message("flexunit.inspections.group");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new FlexUnitInspectionVisitor() {
      @Override
      public void visitJSClass(@NotNull JSClass aClass) {
        FlexUnitSupport support = getFlexUnitSupport(aClass);
        if (support == null || !support.isPotentialTestClass(aClass)) return;
        visitPotentialTestClass(aClass, holder, support);
      }
    };
  }

  protected abstract void visitPotentialTestClass(JSClass aClass, @NotNull final ProblemsHolder holder, FlexUnitSupport support);

}
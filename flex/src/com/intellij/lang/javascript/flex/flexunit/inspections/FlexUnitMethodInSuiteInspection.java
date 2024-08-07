// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitMethodInSuiteInspection extends FlexUnitMethodInspectionBase {

  @Override
  public @NotNull String getShortName() {
    return "FlexUnitMethodInSuiteInspection";
  }

  @Override
  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (support.isSuite((JSClass)method.getParent())) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodinsuite.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      }
    }
  }
}
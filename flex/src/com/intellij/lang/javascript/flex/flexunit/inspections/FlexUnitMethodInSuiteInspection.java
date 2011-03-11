package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodInSuiteInspection extends FlexUnitMethodInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testmethodinsuite.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitMethodInSuiteInspection";
  }

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
package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodHasParametersInspection extends FlexUnitMethodInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testmethodhasparameters.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitMethodHasParametersInspection";
  }

  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (ValidateTypesUtil.hasRequiredParameters(method)) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodhasparameters.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      }
    }
  }
}
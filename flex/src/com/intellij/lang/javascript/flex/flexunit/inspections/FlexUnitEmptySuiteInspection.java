package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitEmptySuiteInspection extends FlexUnitSuiteInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.emptysuite.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitEmptySuiteInspection";
  }


  protected void visitSuite(JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    if (support.isFlexUnit1SuiteSubclass(aClass) || support.isFlexUnit1SuiteSubclass(aClass)) return;
    if (FlexUnitSupport.SUITE_RUNNER.equals(FlexUnitSupport.getCustomRunner(aClass)) && support.getSuiteTestClasses(aClass).isEmpty()) {
      final ASTNode nameIdentifier = aClass.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.emptysuite.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      }
    }

  }
}
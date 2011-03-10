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

public class FlexUnitClassWithNoTestsInspection extends FlexUnitClassInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testclasswithnotests.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitClassWithNoTestsInspection";
  }

  protected void visitPotentialTestClass(JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    if (!support.isTestClass(aClass, false)) return;

    for (JSFunction method : aClass.getFunctions()) {
      if (support.isTestMethod(method)) {
        return;
      }
    }

    final ASTNode nameIdentifier = aClass.findNameIdentifier();
    if (nameIdentifier != null) {
      holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testclasswithnotests.message"),
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
  }
}
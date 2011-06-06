package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.SetElementVisibilityFix;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitClassVisibilityInspection extends FlexUnitClassInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testclassvisibility.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitClassVisibilityInspection";
  }

  protected void visitPotentialTestClass(JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    if (aClass.getAttributeList() == null || aClass.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      final ASTNode nameIdentifier = aClass.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testclassvisibility.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               new SetElementVisibilityFix(aClass, JSAttributeList.AccessType.PUBLIC));
      }
    }
  }
}

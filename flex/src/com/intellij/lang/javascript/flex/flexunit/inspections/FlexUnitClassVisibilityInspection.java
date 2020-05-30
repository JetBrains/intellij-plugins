package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitClassVisibilityInspection extends FlexUnitClassInspectionBase {

  @Override
  @NotNull
  public String getShortName() {
    return "FlexUnitClassVisibilityInspection";
  }

  @Override
  protected void visitPotentialTestClass(@NotNull JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    if (aClass.getAttributeList() == null || aClass.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      final ASTNode nameIdentifier = aClass.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testclassvisibility.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               JSFixFactory.getInstance().createChangeVisibilityFix(aClass, JSAttributeList.AccessType.PUBLIC, null));
      }
    }
  }
}

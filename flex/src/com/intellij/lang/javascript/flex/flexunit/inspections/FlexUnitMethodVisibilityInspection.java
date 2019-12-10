package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodVisibilityInspection extends FlexUnitMethodInspectionBase {

  @Override
  @NotNull
  public String getShortName() {
    return "FlexUnitMethodVisibilityInspection";
  }

  @Override
  protected void visitPotentialTestMethod(@NotNull JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getAttributeList() == null || method.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodvisibility.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               JSFixFactory.getInstance().createChangeVisibilityFix(method, JSAttributeList.AccessType.PUBLIC, null));
      }
    }
  }
}
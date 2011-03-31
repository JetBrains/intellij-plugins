package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.RemoveASTNodeFix;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodIsStaticInspection extends FlexUnitMethodInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testmethodisstatic.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitMethodIsStaticInspection";
  }

  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getAttributeList() != null && method.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        final ASTNode node = method.getAttributeList().getNode().findChildByType(JSTokenTypes.STATIC_KEYWORD);
        assert node != null;
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodisstatic.message"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               new RemoveASTNodeFix("0.is.not.a.legal.name", node) {
                                 @NotNull
                                 @Override
                                 public String getText() {
                                   return FlexBundle.message("flexunit.fix.remove.static.modifier");
                                 }
                               });
      }
    }
  }
}

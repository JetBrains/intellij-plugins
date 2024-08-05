// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.lang.javascript.validation.fixes.RemoveASTNodeFix;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitMethodIsStaticInspection extends FlexUnitMethodInspectionBase {

  @Override
  public @NotNull String getShortName() {
    return "FlexUnitMethodIsStaticInspection";
  }

  @Override
  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getAttributeList() != null && method.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        final ASTNode node = method.getAttributeList().getNode().findChildByType(JSTokenTypes.STATIC_KEYWORD);
        assert node != null;
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodisstatic.message"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               new RemoveASTNodeFix("0.is.not.a.legal.name", node.getPsi()) {
                                 @Override
                                 public @NotNull String getText() {
                                   return FlexBundle.message("flexunit.fix.remove.static.modifier");
                                 }
                               });
      }
    }
  }
}

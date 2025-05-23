// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitMethodVisibilityInspection extends FlexUnitMethodInspectionBase {

  @Override
  public @NotNull String getShortName() {
    return "FlexUnitMethodVisibilityInspection";
  }

  @Override
  protected void visitPotentialTestMethod(@NotNull JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getAttributeList() == null || method.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.problem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodvisibility.message"))
          .fix(JSFixFactory.getInstance().createChangeVisibilityFix(method, JSAttributeList.AccessType.PUBLIC, null))
          .register();
      }
    }
  }
}
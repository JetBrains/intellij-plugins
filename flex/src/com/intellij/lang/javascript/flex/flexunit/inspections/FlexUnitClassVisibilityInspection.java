// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitClassVisibilityInspection extends FlexUnitClassInspectionBase {

  @Override
  public @NotNull String getShortName() {
    return "FlexUnitClassVisibilityInspection";
  }

  @Override
  protected void visitPotentialTestClass(@NotNull JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    if (aClass.getAttributeList() == null || aClass.getAttributeList().getAccessType() != JSAttributeList.AccessType.PUBLIC) {
      final ASTNode nameIdentifier = aClass.findNameIdentifier();
      if (nameIdentifier != null) {
        holder.problem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testclassvisibility.message"))
          .fix(JSFixFactory.getInstance().createChangeVisibilityFix(aClass, JSAttributeList.AccessType.PUBLIC, null))
          .register();
      }
    }
  }
}
